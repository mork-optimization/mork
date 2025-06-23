#################
# CONFIGURATION #
#################
import re

MIN_POINTS = 5

import argparse
import os
import json

from collections import defaultdict

from os.path import join
import numpy as np

from scipy.optimize import curve_fit

import pandas as pd  # data manipulation and analysis
from pandas import DataFrame, Series

import plotly.express as px
import plotly.graph_objects as go

import logging
log = logging.getLogger(__name__)

boring_colors = ["#EDEDE9", "#D6CCC2", "#F5EBE0", "#E3D5CA", "#d6e2e9"]
real_color = "dodgerblue"
best_color = "limegreen"

BEST_ALGORITHM = "bestalg"
BEST_ITERATION = "bestiter"

def format_number_latex(n):
    formatted =  f"{n:+.2g}"
    exponent = re.search(r"e\+?(-?\d+)", formatted)
    if not exponent:
        return formatted
    exponent = exponent.group(1).lstrip("0")
    formatted = re.sub(r"e\+?(-?\d+)", rf"\\cdot 10^{exponent}", formatted)
    return formatted

def format_number_html(n):
    formatted =  f"{n:+.2g}"
    exponent = re.search(r"e\+?(-?\d+)", formatted)
    if not exponent:
        return formatted
    exponent = exponent.group(1).lstrip("0")
    formatted = re.sub(r"e\+?(-?\d+)", rf"&#183;10<sup>{exponent}</sup>", formatted)
    return formatted

class ComplexityFunction(object):
    def __init__(self, name, function, latex, html):
        self.name = name
        self.function = function
        self.latex = latex
        self.html = html


    def f_name_latex(self, a, b):
        return self.latex.replace("a", format_number_latex(a)).replace("b", format_number_latex(b))

    def f_name_html(self, a, b):
        return self.html.replace("a", format_number_html(a)).replace("b", format_number_html(b))


class Fit(object):

    def __init__(self, f: ComplexityFunction, instance_prop, r2, perr, mse, popt, dic, data):
        self.f = f
        self.instance_prop = instance_prop
        self.r2 = r2
        self.perr = perr
        self.popt = popt
        self.mse = mse
        self.dic = dic
        self.data = data

    # Revisar de Salazar: https://ideone.com/xcInVf
    # Podria funcionar mejor que el MSE
    @staticmethod
    def sort(fits: list['Fit']):
        #fits.sort(key=lambda e: abs(e.r2), reverse=True)
        #fits.sort(key=lambda e: e.perr)
        fits.sort(key=lambda e: e.mse)

    @staticmethod
    def get_metric_name():
        return "MSE"

    def get_metric_value(self):
        return self.mse

    def is_better_than(self, other) -> bool:
        return self.mse < other.mse

    def name_html(self):
        return self.f.f_name_html(*self.popt)

    def name_latex(self):
        return self.f.f_name_latex(*self.popt)


def load_df(path: str) -> DataFrame:
    return pd.read_csv(path)


def get_functions() -> list[ComplexityFunction]:
    return [
        ComplexityFunction("Log.", lambda x, a, b: a * np.log(x) + b, r"a \cdot \log(x) b", r"a log(x) b"),
        ComplexityFunction("Linear", lambda x, a, b: a * x + b, r"a \cdot x b", r"ax b"),
        ComplexityFunction("Log. Linear", lambda x, a, b: a * x * np.log(x) + b, r"a \cdot x \log(x) b", r"ax log(x) b"),
        ComplexityFunction("Quadratic", lambda x, a, b: a * x ** 2 + b, r"a \cdot x^2 b", r"ax<sup>2</sup> b"),
        ComplexityFunction("Exponential", lambda x, a, b: a * 2 ** x + b, r"a \cdot 2^x b", r"2<sup>x</sup> b"),
    ]


def get_full_name(stack: list[tuple[str, int]]) -> str:
    return "/".join(frame['name'] for frame in stack)

def fold_profiler_data(path: str) -> DataFrame:
    timestats = []

    for f in os.listdir(path):
        if not f.endswith(".json"):
            continue

        log.debug(f"Processing {f}")
        with open(join(path, f)) as json_file:
            jsondata = json.load(json_file)

        if jsondata['algorithm']['name'] == BEST_ALGORITHM or jsondata['iteration'] == BEST_ITERATION:
            print(f"Skipping file {f}")
            continue

        # Build a list of events and a stack for exclusive time calculation
        events = []
        for i in jsondata['timeData']:
            events.append({'when': i['when'], 'enter': i['enter'], 'clazz': i['clazz'], 'method': i['method']})
        events.sort(key=lambda x: x['when'])

        stack = []

        for i in events:
            name = f"{i['clazz']}::{i['method']}"
            if i['enter']:
                stack.append({'name': name, 'when': i['when'], 'children_time': 0})
            else:
                full_name = get_full_name(stack)
                current = stack.pop()
                if name != current['name']:
                    raise Exception(f"Unexpected stack frame: {name} != {current['name']}")

                exec_time = (i['when'] - current['when']) / 1_000_000  # nanos to millis
                exclusive_time = exec_time - current['children_time']

                # Add exclusive time to parent if exists
                if stack:
                    stack[-1]['children_time'] += exec_time

                parent, child = full_name.rsplit("/", 1) if "/" in full_name else ("", full_name)
                timestats.append({
                    'instance': jsondata['instanceId'],
                    'iter': jsondata['iteration'],
                    'component': full_name,
                    'parent': parent,
                    'child': child,
                    'time': exec_time,
                    'time_exclusive': exclusive_time
                })

    df_forcount = pd.DataFrame(timestats)
    df_forcount.sort_values(by=['instance', 'component', 'iter'], inplace=True)
    df_fortimes = df_forcount.copy()

    df_forcount.drop(columns=['time_exclusive'], inplace=True)
    df_forcount = df_forcount.groupby(['instance', 'component', 'parent', 'child', 'iter'], as_index=False).agg({'time': 'count'})
    df_forcount.drop(columns=['iter'], inplace=True)
    df_forcount = df_forcount.groupby(['instance', 'component', 'parent', 'child'], as_index=False).mean()

    df_fortimes.drop(columns=['iter'], inplace=True)
    df_fortimes.groupby(['instance', 'component', 'parent', 'child'], as_index=False).mean()

    df = df_fortimes.merge(df_forcount, on=['instance', 'component', 'parent', 'child'], suffixes=('', '_count'))

    return df


def prepare_df(df: DataFrame, timestats: DataFrame) -> DataFrame:
    exp_instances = timestats['instance'].unique()
    cloned = df[df['id'].isin(exp_instances)]
    cloned = cloned.sort_values(by=['id'], inplace=False)
    cloned = cloned.drop(['id'], axis=1)
    return cloned


def draw_functions_chart(xy: DataFrame, fits: list[Fit], instance_property, component_name):
    fig = px.line()
    fig.add_scatter(x=xy.index, y=xy['time'], name="$Real$", line=dict(color=real_color))

    for i, fit in enumerate(fits):
        color = boring_colors[i % len(boring_colors)] if i != 0 else best_color
        fig.add_scatter(x=fit.data.x, y=fit.data.y, name=f"${fit.name_latex()}$", line=dict(color=color))
        # print(f"Component {c} - Function {k} - {col} - R2: {r2} - {popt} - {dic['fvec']}")

    fig.update_layout(
        title=rf"$\text{{{component_name} is }}Θ({fits[0].name_latex()})$",
        showlegend=True,
        #legend_title_text="Models",
        legend = dict(
            yanchor="top",
            y=0.99,
            xanchor="left",
            x=0.01,
            font = dict(size = 16),
            #title=dict(font=dict(size=20)),
        ),

        xaxis_title=instance_property,
        yaxis_title="T (ms)"
    )
    fig.show()


def calculate_fitting_func(x: Series, y: Series, f: ComplexityFunction, instance_property: str) -> Fit | None:
    popt, pcov, dic, mesg, _ = curve_fit(f.function, x, y, full_output=True, check_finite=True)
    # https://stackoverflow.com/questions/50371428/scipy-curve-fit-raises-optimizewarning-covariance-of-the-parameters-could-not
    # Curve fit puede fallar
    if np.isnan(pcov).any() or np.isinf(pcov).any():
        return None

    # https://www.geeksforgeeks.org/how-to-return-the-fit-error-in-python-curvefit/
    perr = np.sqrt(np.diag(pcov))
    y_estimated = f.function(x, *popt)
    # residual sum of squares
    ss_res = np.sum((y - y_estimated) ** 2)
    mse = np.mean((y - y_estimated) ** 2)
    # total sum of squares
    ss_tot = np.sum((y - np.mean(y)) ** 2)
    # r-squared
    r2 = 1 - (ss_res / ss_tot)
    data = pd.DataFrame({'x':x, 'y':y_estimated})

    return Fit(f, instance_property, r2, perr, mse, popt, dic, data)

def join_instance_timestats(instances: DataFrame, timestats: DataFrame, component_name: str, instance_property: str):
    x = instances[['id', instance_property]]
    y = timestats[timestats['component'] == component_name][['instance', 'time']]
    xy = x.set_index('id').join(y.set_index('instance'), how='right')
    xy = xy.sort_values(by=[instance_property], inplace=False)
    xy = xy.groupby(instance_property).mean()
    return xy

def calculate_fitting_funcs(instances: DataFrame, timestats: DataFrame, component_name: str, instance_property: str) -> list[Fit]:
    fitting_funcs = get_functions()
    xy = join_instance_timestats(instances, timestats, component_name, instance_property)
    if len(xy) < MIN_POINTS:
        print(f"Instance property {instance_property} has less than {MIN_POINTS} points after grouping, skipping")
        return []

    fits = []
    x = xy.index.to_numpy()
    y = xy['time'].to_numpy()
    for f in fitting_funcs:
        fit = calculate_fitting_func(x, y, f, instance_property)
        if fit:
            fits.append(fit)

    Fit.sort(fits)
    return fits


def find_best_instance_property(instances: DataFrame, timestats: DataFrame, component_name: str) -> list[Fit]:
    best_fits = []

    for instance_property in instances.columns:
        if instance_property == 'id' or instance_property == 'path':
            continue

        fits = calculate_fitting_funcs(instances, timestats, component_name, instance_property)
        if not fits:
            continue
        if not best_fits or fits[0].is_better_than(best_fits[0]):
            best_fits = fits

    return best_fits


def analyze_complexity(instances: DataFrame, timestats: DataFrame):
    treemap_labels = []
    failed_components = []
    for component_name in timestats['component'].unique():
        fits = find_best_instance_property(instances, timestats, component_name)
        # If all fits have failed, skip property
        if not fits:
            failed_components.append(component_name)
            print(f"Failed to calculate complexity of component {component_name}, skipping")
            continue
        best = fits[0]
        xy = join_instance_timestats(instances, timestats, component_name, best.instance_prop)
        draw_functions_chart(xy, fits, best.instance_prop, component_name)

        print(f"Component {component_name} performance predicted as Θ({best.name_latex()}) by {best.instance_prop} - {Fit.get_metric_name()}: {best.get_metric_value()}")
        treemap_labels.append({"component": component_name, "property": best.instance_prop, "function": f"Θ({best.name_html()})", Fit.get_metric_name(): best.get_metric_value()})

    if not treemap_labels:
        print("No components to show, skipping treemap generation")
        return

    treemap_data = timestats.groupby(['component', 'parent', 'child'], as_index=False)['time'].mean()
    # drop failed components
    treemap_data = treemap_data[~treemap_data['component'].isin(failed_components)]
    treemap_data = treemap_data.merge(pd.DataFrame(treemap_labels), on='component')

    treemap_data['child'] = treemap_data['child'].str.replace('::','<br>')
    fig = go.Figure()
    fig.add_trace(go.Treemap(
        ids=treemap_data.component,
        labels=treemap_data.child,
        parents=treemap_data.parent,
        customdata=np.stack((treemap_data.time, treemap_data.property, treemap_data.function, treemap_data[Fit.get_metric_name()]), axis=-1),
        hovertemplate='<b> %{label} </b> <br> Time: %{customdata[0]:.2f} ms <br> Complexity: %{customdata[2]} <br> Where n is: %{customdata[1]} <br> ' + Fit.get_metric_name() + ': %{customdata[3]:.2f}<extra></extra>', # <extra></extra> hides the extra tooltips that contains traceid by default
        marker=dict(
            colors=treemap_data.time,
            colorscale='ylorbr',
            pad=dict(t=50, r=15, b=15, l=15),
            cmin=0,
            cmid=treemap_data.time.mean(),
            showscale=True,
            colorbar=dict(
                title='T (ms)',
                #tickvals=[0, 100, 1000, 10000, 100000, 1000000],
            ),
        ),

        maxdepth=3,
        legend="legend"
    ))
    fig.update_layout(
        uniformtext=dict(minsize=16, mode='show'),
        margin = dict(t=50, l=25, r=25, b=25)
    )

    fig.show()

def main():
    parser = argparse.ArgumentParser(
        description='Creates a set of instances to use during the experimentation',
        epilog='Created for the Mork project, if useful for your research consider citing the original publication')
    parser.add_argument('-p', '--properties', required=False, default="instance_properties.csv", help="CSV Input file containing instance properties.")
    parser.add_argument('-i', '--data', required=False, default="solutions", help="Path to folder which contains profiler data")
    # parser.add_argument('-o', '--output', required=False, default="output", help="Path to output folder.")

    args = parser.parse_args()

    #shutil.rmtree(args.output, ignore_errors=True)
    #os.mkdir(args.output)
    print(f"Loading CSV {args.properties}")
    instances = load_df(args.properties)
    print("Loading profiler data")
    timestats = fold_profiler_data(args.data)
    #print(f"Preparing CSV data")
    #instances = prepare_df(instances, timestats)
    print(f"Analyzing complexity")
    analyze_complexity(instances, timestats)

    print(f"All done, bye!")


if __name__ == '__main__':
    main()
