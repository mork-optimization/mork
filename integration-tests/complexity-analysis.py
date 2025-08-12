#################
# CONFIGURATION #
#################
import re, math
import panel as pn

pn.extension('mathjax', 'plotly', 'katex')

MIN_POINTS = 5
DOUBLE_IMPROVEMENT_ACCEPT_TRESHOLD = 0.5  # double parameter, double improvemenent or reject

import argparse
import os
import json
import warnings

from collections import defaultdict

from os.path import join, isfile
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

PROPERTY_SOURCES = ['time', 'time_exclusive', 'time_count']

from sympy import symbols, simplify, sympify, log, Float, preorder_traversal, sstr, latex, cancel, lambdify, Expr, \
    expand
from sympy.utilities.iterables import iterable
from sympy.printing.mathml import mathml
from sympy.printing.str import StrPrinter

from abc import ABC, abstractmethod

x, a, b = symbols("x a b")
mysymbols = {}


class CustomStrPrinter(StrPrinter):
    def _print_Float(self, expr):
        return '{:.2f}'.format(expr)


# All generated plotly graphs by ID
plots_by_id = defaultdict(lambda: defaultdict(dict))


class ComplexityFunction(ABC):
    @abstractmethod
    def generate_function(self):
        pass

    @abstractmethod
    def name(self):
        pass

    @abstractmethod
    def replace(self, instance_property: list[str], values: list[float]) -> Expr:
        pass

    @abstractmethod
    def __str__(self):
        pass


class ComplexityFunctionSingle(ComplexityFunction):
    def __init__(self, name, calc):
        self._name = name
        self.expr = sympify(calc)

    def generate_function(self):
        return lambdify([x, a, b], self.expr)

    def name(self):
        return self._name

    def replace(self, instance_property: list[str], values: list[float]) -> Expr:
        if len(instance_property) != 1:
            raise Exception("Wrong number of arguments")
        if len(values) != 2:
            raise Exception("Wrong number of arguments")
        return self.expr.subs({x: mysymbols[instance_property[0]], a: values[0], b: values[1]})

    def __str__(self):
        return self.name()


class ComplexityFunSum(ComplexityFunction):
    def __init__(self, compf1: ComplexityFunctionSingle, compf2: ComplexityFunctionSingle):
        self._name = f"{compf1.name()}+{compf2.name()}"
        self.compf1 = compf1
        self.compf2 = compf2

    def name(self):
        return self._name

    def generate_function(self):
        def f(X, av1, bv1):
            x1, x2 = X.T
            y1 = self.compf1.generate_function()(x1, av1, bv1)
            y2 = self.compf2.generate_function()(x2, av1, bv1)
            return y1 + y2

        return f

    def replace(self, instance_property: list[str], values: list[float]) -> Expr:
        if len(instance_property) != 2:
            raise Exception("Wrong number of arguments")
        if len(values) != 2:
            raise Exception("Wrong number of arguments")
        expr1 = self.compf1.expr.subs({x: mysymbols[instance_property[0]], a: values[0], b: values[1]})
        expr2 = self.compf2.expr.subs({x: mysymbols[instance_property[1]], a: values[0], b: values[1]})
        return expr1 + expr2

    def __str__(self):
        return self.name()


class ComplexityFunMult(ComplexityFunction):

    def __init__(self, compf1: ComplexityFunctionSingle, compf2: ComplexityFunctionSingle):
        self._name = f"{compf1.name()}*{compf2.name()}"
        self.compf1 = compf1
        self.compf2 = compf2

    def name(self):
        return self._name

    def generate_function(self):
        def f(X, av1, bv1):
            x1, x2 = X.T
            y1 = self.compf1.generate_function()(x1, av1, bv1)
            y2 = self.compf2.generate_function()(x2, av1, bv1)
            return y1 * y2

        return f

    def replace(self, instance_property: list[str], values: list[float]) -> Expr:
        if len(instance_property) != 2:
            raise Exception("Wrong number of arguments")
        if len(values) != 2:
            raise Exception("Wrong number of arguments")
        expr1 = self.compf1.expr.subs({x: mysymbols[instance_property[0]], a: values[0], b: values[1]})
        expr2 = self.compf2.expr.subs({x: mysymbols[instance_property[1]], a: values[0], b: values[1]})
        return expr1 * expr2

    def __str__(self):
        return self.name()


class Fit(object):
    def __init__(self, f: ComplexityFunction, instance_prop: list[str], perr, mse, popt, dic, data):
        self.f = f
        self.instance_prop = instance_prop
        self.perr = perr
        self.popt = popt
        self.mse = mse
        self.dic = dic
        self.data = data

    # Revisar de Salazar: https://ideone.com/xcInVf
    # Podria funcionar mejor que el MSE
    @staticmethod
    def sort(fits: list['Fit']):
        # fits.sort(key=lambda e: abs(e.r2), reverse=True)
        # fits.sort(key=lambda e: e.perr)
        fits.sort(key=lambda e: e.mse)

    @staticmethod
    def get_metric_name():
        return "MSE"

    def get_metric_value(self):
        return self.mse

    def name_calc(self) -> Expr:
        return self.f

    def name_mathml(self):
        expr = complexity_simplify(self.f)
        return CustomStrPrinter().doprint(expr)

    def __str__(self):
        return f"{self.instance_prop} = {self.name_calc()} (MSE: {self.mse})"


def load_df(path: str) -> DataFrame:
    return pd.read_csv(path)


fitting_functions_single: list[ComplexityFunctionSingle] = [
    ComplexityFunctionSingle("Log.", "a * log(x) + b"),
    ComplexityFunctionSingle("Linear", "a * x + b"),
    ComplexityFunctionSingle("Log. Linear", "a * x * log(x) + b"),
    ComplexityFunctionSingle("Quadratic", "a * x ** 2 + b"),
    ComplexityFunctionSingle("Cubic", "a * x ** 3 + b"),
    ComplexityFunctionSingle("Exponential", "a * 2 ** x + b"),
]


def get_functions_double() -> list[ComplexityFunction]:
    r = []
    for f1 in fitting_functions_single:
        for f2 in fitting_functions_single:
            if f1 != f2:
                r.append(ComplexityFunSum(f1, f2))
                r.append(ComplexityFunMult(f1, f2))
    return r


fitting_functions_double: list[ComplexityFunction] = get_functions_double()


def complexity_formula(d: DataFrame, component_name: str) -> Expr:
    # find its children
    itself = d[(d.component == component_name) & (d.property_source == "time_exclusive")]
    itself_calc_f = itself.calc_function.values[0]

    itself_called_times = d[(d.component == component_name) & (d.property_source == "time_count")]
    itself_called_times_f = itself_called_times.calc_function.values[0]

    children = d[d.parent == component_name].component.unique()
    for child in children:
        # accumulated complexity is number of times called * complexity of child
        child_formula = complexity_formula(d, child)
        child_called_times = d[(d.component == child) & (d.property_source == "time_count")]
        child_called_times_f = child_called_times.calc_function.values[0]
        child_called_times_f = child_called_times_f / itself_called_times_f

        itself_calc_f = itself_calc_f + (child_called_times_f * child_formula)
    return itself_calc_f


def get_full_name(stack: list[tuple[str, int]]) -> str:
    return "/".join(frame['name'] for frame in stack)


def complexity_simplify(original_expr: Expr) -> Expr:
    expr = simplify(original_expr)
    expr_rounded = expr
    # for a in preorder_traversal(expr):
    #    if isinstance(a, Float):
    #        expr_rounded = expr_rounded.subs(a, round(a, 4))

    return expr_rounded


def fold_profiler_data_csv(path: str) -> any:
    with open(path) as csv_file:
        # Header contains instance name algorithm name, iteration number
        instance, algorithm, iteration = csv_file.readline().strip().split(',')
        if algorithm == BEST_ALGORITHM or iteration == BEST_ITERATION:
            print(f"Skipping file {path}")
            return None, None, []

        events = []
        for line in csv_file:
            when, enter, clazz, method = line.strip().split(',')
            events.append({'when': int(when), 'enter': enter == '1', 'clazz': clazz, 'method': method})

        events.sort(key=lambda x: x['when'])
        return instance, algorithm, iteration, events


def fold_profiler_data_json(path: str) -> any:
    with open(path) as json_file:
        jsondata = json.load(json_file)

    instance = jsondata['instanceId']
    algorithm = jsondata['algorithm']['name']
    iteration = jsondata['iteration']
    if algorithm == BEST_ALGORITHM or iteration == BEST_ITERATION:
        print(f"Skipping file {path}")
        return None, None, None, []

    # Build a list of events and a stack for exclusive time calculation
    events = []
    for i in jsondata['timeData']:
        events.append({'when': i['when'], 'enter': i['enter'], 'clazz': i['clazz'], 'method': i['method']})
    events.sort(key=lambda x: x['when'])

    return instance, algorithm, iteration, events


def fold_profiler_data(path: str) -> DataFrame:
    timestats = []

    for f in os.listdir(path):
        fullpath = join(path, f)
        if f.endswith(".csv"):
            instance, algorithm, iteration, events = fold_profiler_data_csv(fullpath)
        elif f.endswith(".json"):
            (instance, algorithm, iteration, events) = fold_profiler_data_json(fullpath)
        else:
            print(f"Skipping file {f}, not a CSV or JSON")
            continue

        if not events:
            print(f"No events returned by loader for file {f}, skipping")
            continue

        stack = []

        for i in events:
            name = f"{i['clazz']}<br>{i['method']}"
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
                    'instance': instance,
                    'iter': iteration,
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
    df_forcount = df_forcount.groupby(['instance', 'component', 'parent', 'child', 'iter'], as_index=False).agg(
        {'time': 'count'})
    df_forcount.drop(columns=['iter'], inplace=True)
    df_forcount = df_forcount.groupby(['instance', 'component', 'parent', 'child'], as_index=False).mean()

    df_fortimes.drop(columns=['iter'], inplace=True)
    df_fortimes = df_fortimes.groupby(['instance', 'component', 'parent', 'child'], as_index=False).mean()

    df = df_fortimes.merge(df_forcount, on=['instance', 'component', 'parent', 'child'], suffixes=('', '_count'))

    return df


def prepare_df(df: DataFrame, timestats: DataFrame) -> DataFrame:
    exp_instances = timestats['instance'].unique()
    cloned = df[df['id'].isin(exp_instances)]
    cloned = cloned.sort_values(by=['id'], inplace=False)
    cloned = cloned.drop(['id'], axis=1)
    return cloned


def generate_f_chart_2d(xy: DataFrame, fit: Fit, component_name, property_source='time'):
    fig = px.line()
    fig.add_scatter(x=xy[fit.instance_prop[0]], y=xy[property_source], name="Real", line=dict(color=real_color))
    fig.add_scatter(x=fit.data.x1, y=fit.data.y, name=fit.name_mathml(), line=dict(color=best_color))

    fig.update_layout(
        title=dict(
            text=rf"{component_name.replace('<br>', '::').replace('/', '→')}<br>{property_source} is Θ({fit.name_mathml()})",
            font=dict(size=12),
        ),
        showlegend=True,
        # legend_title_text="Models",
        legend=dict(
            yanchor="top",
            y=0.99,
            xanchor="left",
            x=0.01,
        ),

        xaxis_title=fit.instance_prop[0],
        yaxis_title="T (ms)"
    )
    return fig


def generate_f_chart_3d(xy: DataFrame, fit: Fit, component_name, property_source='time'):
    fig = px.scatter_3d()
    hovertemplate = f'<b>{fit.instance_prop[0]}</b>: %{{x}}<br><b>{fit.instance_prop[1]}</b>: %{{y}}<br><b>{property_source}</b>: %{{z}}<br>'
    fig.add_scatter3d(x=xy[fit.instance_prop[0]], y=xy[fit.instance_prop[1]], z=xy[property_source], name="Real",
                      line=dict(color=real_color), hovertemplate=hovertemplate)
    fig.add_scatter3d(x=fit.data.x1, y=fit.data.x2, z=fit.data.y, name=fit.name_mathml(), line=dict(color=best_color),
                      hovertemplate=hovertemplate)

    fig.update_layout(
        title=dict(
            text=rf"{component_name.replace('<br>', '::').replace('/', '→')}<br>{property_source} is Θ({fit.name_mathml()})",
            font=dict(size=12),
        ),
        showlegend=True,
        # legend_title_text="Models",
        legend=dict(
            yanchor="top",
            y=0.99,
            xanchor="left",
            x=0.01,
        ),
        scene=dict(
            xaxis=dict(
                title=dict(
                    text=f"{fit.instance_prop[0]}",
                ),
            ),
            yaxis=dict(
                title=dict(
                    text=f"{fit.instance_prop[1]}",
                ),
            ),
            zaxis=dict(
                title=dict(
                    text=f"T (ms)",
                ),
            ),
        ),
    )
    return fig


def generate_f_chart(data: DataFrame, fit: Fit, component_name, property_source='time'):
    xy = data[data['component'] == component_name][[*fit.instance_prop, property_source]].groupby(
        fit.instance_prop).mean()
    xy.reset_index(inplace=True)
    if len(fit.instance_prop) == 1:
        return generate_f_chart_2d(xy, fit, component_name, property_source)
    elif len(fit.instance_prop) == 2:
        return generate_f_chart_3d(xy, fit, component_name, property_source)
    else:
        raise Exception(f"Unsupported number of instance properties: {len(fit.instance_prop)}. Expected 1 or 2.")


def calculate_fitting_func(x: Series, y: Series, f: ComplexityFunction, instance_property: list[str]) -> Fit | None:
    try:
        # https://stackoverflow.com/questions/50371428/scipy-curve-fit-raises-optimizewarning-covariance-of-the-parameters-could-not
        # Curve fit might fail in some cases
        with warnings.catch_warnings():
            warnings.simplefilter("error")  # Turn all warnings into exceptions
            popt, pcov, dic, mesg, _ = curve_fit(f.generate_function(), x, y, full_output=True, check_finite=True,
                                                 maxfev=10000)

            if pcov is None or np.isnan(pcov).any() or np.isinf(pcov).any():
                return None

            # https://www.geeksforgeeks.org/how-to-return-the-fit-error-in-python-curvefit/
            perr = np.sqrt(np.diag(pcov))
            y_estimated = f.generate_function()(x, *popt)
            # residual sum of squares
            mse = np.mean((y - y_estimated) ** 2)
            if x.ndim > 1:
                x_cols = {f'x{i + 1}': x[:, i] for i in range(x.shape[1])}
                x_cols['y'] = y_estimated
                data = pd.DataFrame(x_cols)
            else:
                data = pd.DataFrame({'x1': x, 'y': y_estimated})

            f = f.replace(instance_property, popt)
            f = expand(f)
            return Fit(f, instance_property, perr, mse, popt, dic, data)

    except Warning as warn:
        if warn.args[0] == 'overflow encountered in power':
            # Some functions such as exponential can easily overflow, this is expected, and the function will be discarded
            return None
        elif warn.args[0] == 'Covariance of the parameters could not be estimated':
            # Bad / failed fit
            return None
        else:
            logging.warning(f"{f}: {warn}")
            return None
    except RuntimeError as err:
        logging.warning(f"{f}: {err}")
        return None


def calculate_fitting_funcs(data: DataFrame, component: str, feature: str, property_src: str) -> list[Fit]:
    xy = data[data['component'] == component][[feature, property_src]]
    # Instance features may have duplicated values (ex: graph density with different results). Average them.
    xy = xy.groupby(feature).mean()

    x, y = xy.index.to_numpy(), xy[property_src].to_numpy()

    fits = []
    for f in fitting_functions_single:
        fit = calculate_fitting_func(x, y, f, [feature])
        if fit:
            fits.append(fit)

    return fits


def calculate_fitting_funcs2(data: DataFrame, component: str, feature1: str, feature2: str,
                             property_src: str) -> list[Fit]:
    xy = data[data['component'] == component][[feature1, feature2, property_src]]
    # Instance features may have duplicated values (ex: graph density with different results). Average them.
    xy = xy.groupby([feature1, feature2]).mean()
    if len(xy) < MIN_POINTS:
        print(
            f"Instance property pair ({feature1}, {feature2}) has less than {MIN_POINTS} points after grouping, skipping")
        return []

    x, y = xy.index.to_numpy(), xy[property_src].to_numpy()
    x = np.array([*x])  # Replaces array of tuples with 2d np array

    fits = []
    for f in fitting_functions_double:
        fit = calculate_fitting_func(x, y, f, [feature1, feature2])
        if fit:
            fit.mse = fit.mse / DOUBLE_IMPROVEMENT_ACCEPT_TRESHOLD
            fits.append(fit)

    return fits


def find_best_instance_property(features: list[str], data: DataFrame, component: str, prop_src: str) -> list[Fit]:
    fits_single: list[Fit] = []
    for feature in features:
        fits_single += calculate_fitting_funcs(data, component, feature, prop_src)

    Fit.sort(fits_single)
    fits_double: list[Fit] = []
    for i in range(len(features)):
        for j in range(i + 1, len(features)):
            fits_double += calculate_fitting_funcs2(data, component, features[i], features[j], prop_src)

    Fit.sort(fits_double)

    # Return best 3 of each type
    fits = fits_single[:3] + fits_double[:3]
    Fit.sort(fits)
    return fits


def try_analyze_all_property_sources(instance_features: list[str], data: DataFrame):
    treemap_labels = []
    failed_components = []
    for component_name in data['component'].unique():
        complexities = {}
        for property_source in PROPERTY_SOURCES:
            complexities[property_source] = find_best_instance_property(instance_features, data, component_name,
                                                                        property_source)
            # If all fits have failed, skip property
            if not complexities[property_source]:
                failed_components.append(component_name)
                print(f"Failed to calculate complexity of component {component_name}, skipping")
                return None
            best = complexities[property_source][0]
            for i in range(len(complexities[property_source])):
                current = complexities[property_source][i]
                f_chart = generate_f_chart(data, current, component_name, property_source)
                plots_by_id[component_name][property_source][current.name_calc()] = f_chart

            print(
                f"{component_name} ({property_source}) --> Θ({best.name_mathml()}) - {Fit.get_metric_name()}: {best.get_metric_value()}")
            treemap_labels.append(
                {"component": component_name, "property_source": property_source, "calc_function": best.name_calc(),
                 Fit.get_metric_name(): best.get_metric_value()})

    if not treemap_labels:
        print("No components to show, skipping treemap generation")
        return None

    tree_df = data.groupby(['component', 'parent', 'child'], as_index=False)['time'].mean()
    all_formulas = pd.DataFrame(treemap_labels)
    # drop failed components
    all_formulas = all_formulas[~all_formulas['component'].isin(failed_components)]
    all_formulas = all_formulas.merge(tree_df, on='component')

    for component_name in data['component'].unique():
        # Calculate the combined complexity of each component
        c_combined = complexity_formula(all_formulas, component_name)
        c_combined_simpl = complexity_simplify(c_combined)

        simple = all_formulas[(all_formulas['component'] == component_name) & (all_formulas.property_source == "time")]
        simple_metric_v = simple[Fit.get_metric_name()].values[0]
        c_simple = simple.calc_function.values[0]
        c_simple_simpl = complexity_simplify(c_simple)

        simple_exclusive = all_formulas[
            (all_formulas['component'] == component_name) & (all_formulas.property_source == "time_exclusive")]
        simple_exclusive_metric_v = simple_exclusive[Fit.get_metric_name()].values[0]
        c_simple_exclusive = simple_exclusive.calc_function.values[0]
        c_simpl_simple_exclusive = complexity_simplify(c_simple_exclusive)

        mse_comb = mse(c_combined_simpl, instance_features, data, component_name, 'time')
        mse_simpl = mse(c_simple_simpl, instance_features, data, component_name, 'time')
        mse_simpl_excl = mse(c_simpl_simple_exclusive, instance_features, data, component_name, 'time_exclusive')

        print(f"Component {component_name} complexity: ")
        print(f" - Combined: {c_combined}")
        print(f" - Combined (MSE: {mse_comb}): {c_combined_simpl}")
        print(f" - Simple: {c_simple}")
        print(f" - Black box (MSE: {mse_simpl}): {c_simple_simpl}")
        print(f" - Simple Exclusive: {c_simple_exclusive}")
        print(f" - Black box exclusive (MSE: {mse_simpl_excl}): {c_simpl_simple_exclusive}")
        formatter = CustomStrPrinter().doprint
        tree_df.loc[tree_df['component'] == component_name, 'f_comb'] = formatter(c_combined_simpl)
        tree_df.loc[tree_df['component'] == component_name, 'f_simpl'] = formatter(c_simple_simpl)
        tree_df.loc[tree_df['component'] == component_name, 'f_simpl_excl'] = formatter(c_simpl_simple_exclusive)
        tree_df.loc[tree_df['component'] == component_name, Fit.get_metric_name()] = simple_metric_v
        tree_df.loc[tree_df['component'] == component_name, 'mse_comb'] = mse_comb
        tree_df.loc[tree_df['component'] == component_name, 'mse_simpl'] = mse_simpl
        tree_df.loc[tree_df['component'] == component_name, 'mse_simpl_excl'] = mse_simpl_excl

    return tree_df


def mse(formula, instance_features, data: DataFrame, component_name: str, property_source: str) -> float:
    xy = data[data['component'] == component_name][['instance', property_source, *instance_features]]
    xy = xy.groupby('instance', as_index=False).mean()

    def eval_formula(row):
        subs = {feature: row[feature] for feature in instance_features}
        return formula.evalf(subs=subs)

    xy['y_estimated'] = xy.apply(eval_formula, axis=1)

    mse = np.mean((xy[property_source] - xy.y_estimated) ** 2)
    return float(mse)  # np uses 'floating' type


def analyze_complexity(root_component_id: str, instance_features: list[str], data: DataFrame):
    treemap_data = try_analyze_all_property_sources(instance_features, data)

    if treemap_data is None:
        print("[ERROR] At least one component failed to estimate, cannot generate treemap.")
        return None

    # Aggregate data for each component
    per_component = data.drop(columns=['instance', 'parent', 'child']).groupby(['component'], as_index=False).mean()
    per_component['time_total'] = per_component['time'] * per_component['time_count']
    per_component['time_total_excl'] = per_component['time_exclusive'] * per_component['time_count']
    per_component['time_total_excl_scaled'] = np.sqrt(per_component['time_total_excl'])
    per_component.drop(columns=['time'], inplace=True)
    treemap_data = pd.merge(treemap_data, per_component, on='component')
    fig = go.Figure()
    fig.add_trace(go.Treemap(
        ids=treemap_data.component,
        values=treemap_data.time_total_excl_scaled,
        branchvalues="remainder",
        labels=treemap_data.child,
        parents=treemap_data.parent,
        customdata=np.stack(
            (treemap_data.time_total, treemap_data.f_comb, treemap_data.f_simpl, treemap_data.f_simpl_excl,
             treemap_data[Fit.get_metric_name()], treemap_data.mse_comb, treemap_data.mse_simpl,
             treemap_data.mse_simpl_excl, treemap_data.time, treemap_data.time_count, treemap_data.time_total_excl,
             treemap_data.time_exclusive), axis=-1),
        hovertemplate='<b>%{label} </b> <br> Total T(ms): %{customdata[0]:.2f} (%{customdata[8]:.2f} * %{customdata[9]:.2f}) <br> Exclusive T(ms): %{customdata[10]:.2f} (%{customdata[11]:.2f} * %{customdata[9]:.2f})<br> Global (MSE: %{customdata[5]:.1f}): %{customdata[1]} <br> Combined (MSE: %{customdata[6]:.1f}): %{customdata[2]} <br> Exclusive (MSE: %{customdata[7]:.1f}): %{customdata[3]}<extra></extra>',
        # <extra></extra> hides the extra tooltips that contains traceid by default
        marker=dict(
            colors=treemap_data.time,
            colorscale='RdYlGn_r',
            pad=dict(t=50, r=15, b=15, l=15),
            cmin=0,
            cmax=treemap_data.time_total_excl_scaled.max(),
            showscale=True,
            colorbar=dict(
                title='sqrt(T (ms))',
                # tickvals=[0, 100, 1000, 10000, 100000, 1000000],
            ),
        ),

        maxdepth=3,
        legend="legend"
    ))

    heatmap_pane = pn.pane.Plotly(fig)
    selectA = pn.widgets.Select(name='Type')
    selectB = pn.widgets.Select(name='Ranked fits')
    scatter_pane = pn.Accordion()
    last_selected_component = ""
    last_selected_type = ""

    @pn.depends(selectA.param.value)
    def on_selectA_change(value):
        print("On selectA change:", value)
        if not value:
            return
        update_selector_paneB(value)

    @pn.depends(selectB.param.value)
    def on_selectB_change(value):
        if not value:
            return
        update_scatter_plots(scatter_pane, value)

    def update_selector_paneA(component_id):
        nonlocal last_selected_component, last_selected_type
        print("Selected component: ", component_id)
        last_selected_component = component_id
        new_options = {}
        for type in plots_by_id[last_selected_component].keys():
            new_options[f"{type}: {last_selected_component}"] = type
        selectA.options = new_options
        selectA.value = next(iter(selectA.options.values()))
        update_selector_paneB(selectA.value)

    def update_selector_paneB(type_id):
        nonlocal last_selected_component, last_selected_type
        print("Selected type: ", type_id)
        last_selected_type = type_id
        selectB.options = list(plots_by_id[last_selected_component][last_selected_type])
        selectB.value = selectB.options[0]

    @pn.depends(heatmap_pane.param.click_data)
    def on_click_heatmap(event):
        if not event:
            return
        update_selector_paneA(event['points'][0]['id'])

    def update_scatter_plots(scatter_pane, fit_id):
        nonlocal last_selected_component, last_selected_type
        print("Selected fit id: ", fit_id)
        scatter_pane.clear()
        k = last_selected_type
        v = plots_by_id[last_selected_component][last_selected_type][fit_id]
        scatter_pane.append((k, v))
        scatter_pane.active = [0]

    column_pane = pn.Column(heatmap_pane, on_click_heatmap, selectA, on_selectA_change, selectB, on_selectB_change)
    update_selector_paneA(root_component_id)

    root_panel = pn.FlexBox(column_pane, scatter_pane)
    return root_panel


def main():
    parser = argparse.ArgumentParser(
        description='Creates a set of instances to use during the experimentation',
        epilog='Created for the Mork project, if useful for your research consider citing the original publication')
    parser.add_argument('-p', '--properties', required=False, default="instance_properties.csv",
                        help="CSV Input file containing instance properties.")
    parser.add_argument('-i', '--data', required=False, default="solutions",
                        help="Path to folder which contains profiler data. Data can be stored in either CSV files or JSON, see docs for more details on the format.")

    args = parser.parse_args()

    if not isfile(args.properties):
        print(f"File {args.properties} does not exist, please provide a valid CSV file with instance properties")
        exit(1)

    print(f"Loading CSV {args.properties}")
    instances = load_df(args.properties)

    instance_features = instances.columns.values
    instance_features = np.delete(instance_features,
                                  np.where((instance_features == "id") | (instance_features == "path")))

    for feature in instance_features:
        if instances[feature].nunique() < MIN_POINTS:
            print(f"Feature {feature} has less than {MIN_POINTS} unique values, skipping")
            instance_features = np.delete(instance_features, np.where(instance_features == feature))
            instances.drop(columns=[feature], inplace=True)
        else:
            mysymbols[feature] = symbols(feature)

    print("Loading profiler data")
    timestats = fold_profiler_data(args.data)

    root_components = timestats[timestats['parent'] == ""].component.unique()
    if len(root_components) != 1:
        raise Exception(f"Expected exactly one root component, found {len(root_components)}")
    root_component = root_components[0]

    data = pd.merge(timestats, instances, left_on='instance', right_on='id', how='left')
    data.drop(columns=['id', 'path'], inplace=True)

    print(f"Analyzing complexity")
    root_panel = analyze_complexity(root_component, instance_features, data)

    if not root_panel:
        print("Complexity analysis failed, review logs and open a Github issue if you suspect this is a bug")
        exit(1)

    if __name__ == "__main__":
        root_panel.show(Threaded=True)
    else:
        root_panel.servable()


main()
