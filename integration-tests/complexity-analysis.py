#################
# CONFIGURATION #
#################
import re

MIN_POINTS = 5

import argparse
import os
import json
import warnings

from collections import defaultdict

from os.path import join
import numpy as np

from scipy.optimize import curve_fit

import pandas as pd  # data manipulation and analysis
from pandas import DataFrame, Series

import plotly.express as px
import plotly.graph_objects as go

import logging

boring_colors = ["#EDEDE9", "#D6CCC2", "#F5EBE0", "#E3D5CA", "#d6e2e9"]
real_color = "dodgerblue"
best_color = "limegreen"

BEST_ALGORITHM = "bestalg"
BEST_ITERATION = "bestiter"

from sympy import symbols, simplify, log, Float, preorder_traversal, sstr

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
    def __init__(self, name, function, latex, html, calc):
        self.name = name
        self.function = function
        self.latex = latex
        self.html = html
        self.calc = calc


    def f_name_latex(self, a, b):
        return self.latex.replace("a", format_number_latex(a)).replace("b", format_number_latex(b))

    def f_name_html(self, a, b):
        return self.html.replace("a", format_number_html(a)).replace("b", format_number_html(b))

    def f_name_calc(self, a, b):
        return self.calc.replace("a", str(a)).replace("b", str(b))


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

    def name_calc(self):
        return self.f.f_name_calc(*self.popt)

    def name_html(self):
        return self.f.f_name_html(*self.popt)

    def name_latex(self):
        return self.f.f_name_latex(*self.popt)


def load_df(path: str) -> DataFrame:
    return pd.read_csv(path)


def get_functions() -> list[ComplexityFunction]:
    return [
        ComplexityFunction("Log.", lambda x, a, b: a * np.log(x) + b, r"a \cdot \log(x) b", r"a log(x) b", "a * log(x) + b"),
        ComplexityFunction("Linear", lambda x, a, b: a * x + b, r"a \cdot x b", r"ax b", "a * x + b"),
        ComplexityFunction("Log. Linear", lambda x, a, b: a * x * np.log(x) + b, r"a \cdot x \log(x) b", r"ax log(x) b", "a * x * log(x) + b"),
        ComplexityFunction("Quadratic", lambda x, a, b: a * x ** 2 + b, r"a \cdot x^2 b", r"ax<sup>2</sup> b", "a * x ** 2 + b"),
        ComplexityFunction("Exponential", lambda x, a, b: a * 2 ** x + b, r"a \cdot 2^x b", r"2<sup>x</sup> b", "a * 2 ** x + b"),
    ]

def complexity_formula(d: DataFrame, component_name: str) -> str:
    # find its children
    itself = d[(d.component==component_name) & (d.property_source=="time_exclusive")]
    itself_calc_f = itself.calc_function.values[0]
    itself_instance_property = itself.instance_property.values[0]
    itself_calc_f = itself_calc_f.replace("x", itself_instance_property)

    itself_called_times = d[(d.component==component_name) & (d.property_source=="time_count")]
    itself_called_times_f = itself_called_times.calc_function.values[0]
    itself_called_times_instance_property = itself_called_times.instance_property.values[0]
    itself_called_times_f = itself_called_times_f.replace("x", itself_called_times_instance_property)

    # El bug es que asumo que la f de cuantas veces se llama el child es relativa desde el padre
    # Cuando la estoy calculando de forma absolute
    # ¿Dividir child entre parent deberia dar el resultado correcto? Poner como variable y pedirle que simplifique luego
    children = d[d.parent==component_name].component.unique()
    for child in children:
        # accumulated complexity is number of times called * complexity of child
        child_formula = complexity_formula(d, child)
        child_called_times = d[(d.component==child) & (d.property_source=="time_count")]
        child_called_times_f = child_called_times.calc_function.values[0]
        child_called_times_instance_property = child_called_times.instance_property.values[0]
        child_called_times_f = child_called_times_f.replace("x", child_called_times_instance_property)
        child_called_times_f = f"({child_called_times_f}) / ({itself_called_times_f})"

        itself_calc_f += f" + ({child_called_times_f}) * ({child_formula})"
    return itself_calc_f

def get_full_name(stack: list[tuple[str, int]]) -> str:
    return "/".join(frame['name'] for frame in stack)

def complexity_simplify(original_expr: str) -> str:
    expr = simplify(original_expr)
    expr_rounded = expr
    for a in preorder_traversal(expr):
        if isinstance(a, Float):
            expr_rounded = expr_rounded.subs(a, round(a, 2))
            # rounded1 = round(a, 1)
            # rounded0 = round(a, 0)
            # if abs(rounded1 == rounded0) < 1e-3:
            #     expr_rounded = expr_rounded.subs(a, rounded0)
            # else:
            #     expr_rounded = expr_rounded.subs(a, rounded1)

    return expr_rounded

def fold_profiler_data_csv(path: str) -> any:
    with open(path) as csv_file:
        # Header contains instance name algorithm name, iteration number
        instance, algorithm, iteration = csv_file.readline().strip().split(',')
        if algorithm == BEST_ALGORITHM or iteration == BEST_ITERATION:
            print(f"Skipping file {path}")
            return []

        events = []
        for line in csv_file:
            when, enter, clazz, method = line.strip().split(',')
            events.append({'when': int(when), 'enter': enter == '1', 'clazz': clazz, 'method': method})

        events.sort(key=lambda x: x['when'])
        return events


def fold_profiler_data_json(path: str) -> any:
    with open(path) as json_file:
        jsondata = json.load(json_file)

    if jsondata['algorithm']['name'] == BEST_ALGORITHM or jsondata['iteration'] == BEST_ITERATION:
        print(f"Skipping file {path}")
        return []

    # Build a list of events and a stack for exclusive time calculation
    events = []
    for i in jsondata['timeData']:
        events.append({'when': i['when'], 'enter': i['enter'], 'clazz': i['clazz'], 'method': i['method']})
    events.sort(key=lambda x: x['when'])

    return events


def fold_profiler_data(path: str) -> DataFrame:
    timestats = []

    for f in os.listdir(path):
        fullpath = join(path, f)
        if f.endswith(".csv"):
            events = fold_profiler_data_csv(fullpath)
        elif f.endswith(".json"):
            events = fold_profiler_data_json(fullpath)
        else:
            print(f"Skipping file {f}, not a CSV or JSON")
            continue

        if not events:
            print(f"No events returned by loader for file {f}, skipping")
            continue


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


def draw_functions_chart(xy: DataFrame, fits: list[Fit], instance_property, component_name, property_source='time'):
    fig = px.line()
    fig.add_scatter(x=xy.index, y=xy[property_source], name="$Real$", line=dict(color=real_color))

    for i, fit in enumerate(fits):
        color = boring_colors[i % len(boring_colors)] if i != 0 else best_color
        fig.add_scatter(x=fit.data.x, y=fit.data.y, name=f"${fit.name_latex()}$", line=dict(color=color))
        # print(f"Component {c} - Function {k} - {col} - R2: {r2} - {popt} - {dic['fvec']}")

    fig.update_layout(
        title=rf"$\text{{{component_name} - {property_source} is }}Θ({fits[0].name_latex()})$",
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
    try:
        # https://stackoverflow.com/questions/50371428/scipy-curve-fit-raises-optimizewarning-covariance-of-the-parameters-could-not
        # Curve fit might fail in some cases
        with warnings.catch_warnings():
            warnings.simplefilter("error")  # Turn all warnings into exceptions
            popt, pcov, dic, mesg, _ = curve_fit(f.function, x, y, full_output=True, check_finite=True)

            if pcov is None or np.isnan(pcov).any() or np.isinf(pcov).any():
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

            # If popt is near whole numbers, round it
            for i, p in enumerate(popt):
                if abs(p - round(p)) < 1e-6:
                    popt[i] = round(p)

            return Fit(f, instance_property, r2, perr, mse, popt, dic, data)

    except Warning as warn:
        if warn.args[0] == 'overflow encountered in power':
            # Some functions such as exponential can easily overflow, this is expected and the function will be discarded
            return None
        else:
            logging.warning(f"Warning during curve fitting: {warn}")
            return None



def join_instance_timestats(instances: DataFrame, timestats: DataFrame, component_name: str, instance_property: str, property_source: str = 'time') -> DataFrame:
    x = instances[['id', instance_property]]
    y = timestats[timestats['component'] == component_name][['instance', property_source]]
    xy = x.set_index('id').join(y.set_index('instance'), how='right')
    xy = xy.sort_values(by=[instance_property], inplace=False)
    xy = xy.groupby(instance_property).mean()
    return xy

def calculate_fitting_funcs(instances: DataFrame, timestats: DataFrame, component_name: str, instance_property: str, property_source: str) -> list[Fit]:
    fitting_funcs = get_functions()
    xy = join_instance_timestats(instances, timestats, component_name, instance_property, property_source)
    if len(xy) < MIN_POINTS:
        print(f"Instance property {instance_property} has less than {MIN_POINTS} points after grouping, skipping")
        return []

    fits = []
    x = xy.index.to_numpy()
    if not property_source in xy.columns:
        raise Exception(f"Property source {property_source} not found in data, skipping")

    y = xy[property_source].to_numpy()
    if (y == y[0]).all():
        # Constant makes R2 and other metrics fail, add small value to first point to avoid all points being strictly equal
        y[0] -= 1e-6

    for f in fitting_funcs:
        fit = calculate_fitting_func(x, y, f, instance_property)
        if fit:
            fits.append(fit)

    Fit.sort(fits)
    return fits


def find_best_instance_property(instances: DataFrame, timestats: DataFrame, component_name: str, property_source: str) -> list[Fit]:
    best_fits = []

    for instance_property in instances.columns:
        if instance_property == 'id' or instance_property == 'path':
            continue

        fits = calculate_fitting_funcs(instances, timestats, component_name, instance_property, property_source)
        if not fits:
            continue
        if not best_fits or fits[0].is_better_than(best_fits[0]):
            best_fits = fits

    return best_fits


def try_analyze_all_property_sources(instances: DataFrame, timestats: DataFrame):
    treemap_labels = []
    failed_components = []
    for component_name in timestats['component'].unique():
        complexities = {}
        sources = ['time', 'time_exclusive', 'time_count']
        for property_source in sources:
            complexities[property_source] = find_best_instance_property(instances, timestats, component_name, property_source)
            # If all fits have failed, skip property
            if not complexities[property_source]:
                failed_components.append(component_name)
                print(f"Failed to calculate complexity of component {component_name}, skipping")
                return None
            best = complexities[property_source][0]
            xy = join_instance_timestats(instances, timestats, component_name, best.instance_prop, property_source)
            draw_functions_chart(xy, complexities[property_source], best.instance_prop, component_name, property_source)

            print(f"Component {component_name}, source {property_source} performance predicted as Θ({best.name_latex()}) by {best.instance_prop} - {Fit.get_metric_name()}: {best.get_metric_value()}")
            treemap_labels.append({"component": component_name, "property_source":property_source, "instance_property": best.instance_prop, "calc_function": f"{best.name_calc()}","html_function": f"Θ({best.name_html()})", Fit.get_metric_name(): best.get_metric_value()})


    if not treemap_labels:
        print("No components to show, skipping treemap generation")
        return None

    treemap_data = timestats.groupby(['component', 'parent', 'child'], as_index=False)['time'].mean()
    all_formulas = pd.DataFrame(treemap_labels)
    # drop failed components
    all_formulas = all_formulas[~all_formulas['component'].isin(failed_components)]
    all_formulas = all_formulas.merge(treemap_data, on='component')

    all_formulas['child'] = all_formulas['child'].str.replace('::','<br>')
    # Replace non-alphanumeric characters with underscores
    all_formulas['instance_property'] = all_formulas['instance_property'].str.replace('\W+','_', regex=True)

    for ip in all_formulas['instance_property'].unique():
        # Create a symbol for each instance property
        # This allows us to use the instance property in the complexity formulas directly
        exec(f'{ip} = symbols("{ip}")')

    for component_name in timestats['component'].unique():
        # Calculate the combined complexity of each component
        c_combined = complexity_formula(all_formulas, component_name)
        c_simpl_combined = complexity_simplify(eval(c_combined))

        simple = all_formulas[(all_formulas['component']==component_name) & (all_formulas.property_source=="time")]
        simple_instance_property = simple.instance_property.values[0]
        simple_metric_v = simple[Fit.get_metric_name()].values[0]
        c_simple = simple.calc_function.values[0].replace("x", simple_instance_property)
        c_simpl_simple = complexity_simplify(eval(c_simple))

        simple_exclusive = all_formulas[(all_formulas['component']==component_name) & (all_formulas.property_source=="time_exclusive")]
        simple_exclusive_instance_property = simple_exclusive.instance_property.values[0]
        simple_exclusive_metric_v = simple_exclusive[Fit.get_metric_name()].values[0]
        c_simple_exclusive = simple_exclusive.calc_function.values[0].replace("x", simple_exclusive_instance_property)
        c_simpl_simple_exclusive = complexity_simplify(eval(c_simple_exclusive))

        mse_comb = mse(c_simpl_combined, instances, timestats, component_name, 'time')
        mse_simpl = mse(c_simpl_simple, instances, timestats, component_name, 'time')
        mse_simpl_excl = mse(c_simpl_simple_exclusive, instances, timestats, component_name, 'time_exclusive')

        print(f"Component {component_name} complexity: ")
        print(f" - Combined: {c_combined}")
        print(f" - Combined (MSE: {mse_comb}): {c_simpl_combined}")
        print(f" - Simple: {c_simple}")
        print(f" - Black box (MSE: {mse_simpl}): {c_simpl_simple}")
        print(f" - Simple Exclusive: {c_simple_exclusive}")
        print(f" - Black box exclusive (MSE: {mse_simpl_excl}): {c_simpl_simple_exclusive}")

        treemap_data.loc[treemap_data['component'] == component_name, 'f_comb'] = sstr(c_simpl_combined)
        treemap_data.loc[treemap_data['component'] == component_name, 'f_simpl'] = sstr(c_simpl_simple)
        treemap_data.loc[treemap_data['component'] == component_name, 'f_simpl_excl'] = sstr(c_simpl_simple_exclusive)
        treemap_data.loc[treemap_data['component'] == component_name, Fit.get_metric_name()] = simple_metric_v
        treemap_data.loc[treemap_data['component'] == component_name, 'mse_comb'] = mse_comb
        treemap_data.loc[treemap_data['component'] == component_name, 'mse_simpl'] = mse_simpl
        treemap_data.loc[treemap_data['component'] == component_name, 'mse_simpl_excl'] = mse_simpl_excl

    return treemap_data

def mse(formula, instances: DataFrame, timestats: DataFrame, component_name: str, property_source: str) -> float:
    x = instances.drop(columns=['path'], inplace=False)
    y = timestats[timestats['component'] == component_name][['instance', property_source]]
    y = y.groupby('instance', as_index=False).mean()
    xy = x.set_index('id').join(y.set_index('instance'), how='right')
    # Column names without id and property_source
    instance_properties = [col for col in xy.columns if col not in ['id', property_source]]
    def eval_formula(row):
        subs = {prop: row[prop] for prop in instance_properties}
        return formula.evalf(subs=subs)

    xy['y_estimated'] = xy.apply(eval_formula, axis=1)

    mse = np.mean((xy[property_source] - xy.y_estimated) ** 2)
    return mse


def analyze_complexity(instances: DataFrame, timestats: DataFrame):
    treemap_data = try_analyze_all_property_sources(instances, timestats)
    if treemap_data is None:
        print("[ERROR] At least one component failed to estimate, cannot generate treemap.")
        return

    fig = go.Figure()
    fig.add_trace(go.Treemap(
        ids=treemap_data.component,
        labels=treemap_data.child,
        parents=treemap_data.parent,
        customdata=np.stack((treemap_data.time, treemap_data.f_comb, treemap_data.f_simpl, treemap_data.f_simpl_excl, treemap_data[Fit.get_metric_name()], treemap_data.mse_comb, treemap_data.mse_simpl, treemap_data.mse_simpl_excl), axis=-1),
        hovertemplate='<b> %{label} </b> <br> Time: %{customdata[0]:.2f} ms <br> Global (MSE: %{customdata[5]:.1f}): %{customdata[1]} <br> Combined (MSE: %{customdata[6]:.1f}): %{customdata[2]} <br> Exclusive (MSE: %{customdata[7]:.1f}): %{customdata[3]} <br>' + Fit.get_metric_name() + ': %{customdata[4]:.2f}<extra></extra>', # <extra></extra> hides the extra tooltips that contains traceid by default
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
    parser.add_argument('-i', '--data', required=False, default="solutions", help="Path to folder which contains profiler data. Data can be stored in either CSV files or JSON, see docs for more details on the format.")
    # parser.add_argument('-o', '--output', required=False, default="output", help="Path to output folder.")

    args = parser.parse_args()

    #shutil.rmtree(args.output, ignore_errors=True)
    #os.mkdir(args.output)
    print(f"Loading CSV {args.properties}")
    instances = load_df(args.properties)
    print("Loading profiler data")
    timestats = fold_profiler_data(args.data)
    print(f"Analyzing complexity")
    analyze_complexity(instances, timestats)

    print(f"All done, bye!")


if __name__ == '__main__':
    main()
