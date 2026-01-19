#################
# CONFIGURATION #
#################
import panel as pn
from alive_progress import alive_bar

pn.extension('mathjax', 'plotly', 'katex')

# Maximum number of box depth when stacking in the tree plot
# Lower levels than MAX_DEPTH are hidden at first, but are shown
# later if MAX_DEPTH changes when their parent component is clicked
MAX_DEPTH=5

# Minimum number of different values than an instance feature must have
# Instance features that do not have enough values will be discarded
MIN_POINTS = 5

# Penalization applied when using two instance features instead of one, to avoid overfitting.
# If using two instance features do not improve by at least 20% <--> 0. value to the best single feature,
# they are ranked lower or even discarded
DOUBLE_IMPROVEMENT_ACCEPT_TRESHOLD = 0.8


NEW_LINE = "<br>"  # HTML line break, used in component names
# Minimum value that a double can be before it is considered equal to 0.
MIN_VALUE = 0.000_000_001

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

from sympy import symbols, simplify, sympify, lambdify, Expr, \
    expand
from sympy.printing.mathml import mathml
from sympy.utilities.mathml import c2p
from sympy.printing.str import StrPrinter

from abc import ABC, abstractmethod

x, a, b, c = symbols("x a b c")
mysymbols = {}


class CustomStrPrinter(StrPrinter):
    def _print_Float(self, expr):
        return '{:.2g}'.format(expr)


# All generated plotly graphs by ID
fits_by_id = defaultdict(lambda: defaultdict(dict))


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
        self.expr_orig = sympify(calc)
        self.expr = a * self.expr_orig + b

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
        def f(X, av1, bv1, cv1):
            x1, x2 = X.T
            y1 = self.compf1.generate_function()(x1, av1, cv1 / 2)
            y2 = self.compf2.generate_function()(x2, bv1, cv1 / 2)
            return y1 + y2

        return f

    def replace(self, instance_property: list[str], values: list[float]) -> Expr:
        if len(instance_property) != 2:
            raise Exception("Wrong number of arguments")
        if len(values) != 3:
            raise Exception("Wrong number of arguments")
        expr1 = self.compf1.expr_orig.subs({x: mysymbols[instance_property[0]]})
        expr2 = self.compf2.expr_orig.subs({x: mysymbols[instance_property[1]]})
        expr_comb = a * expr1 + b * expr2 + c
        expr_comb = expr_comb.subs({a: values[0], b: values[1], c: values[2]})
        return expr_comb

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
            y1 = self.compf1.generate_function()(x1, 1, 0)
            y2 = self.compf2.generate_function()(x2, 1, 0)
            return av1 * y1 * y2 + bv1

        return f

    def replace(self, instance_property: list[str], values: list[float]) -> Expr:
        if len(instance_property) != 2:
            raise Exception("Wrong number of arguments")
        if len(values) != 2:
            raise Exception("Wrong number of arguments")
        expr1 = self.compf1.expr.subs({x: mysymbols[instance_property[0]], a: 1, b: 0})
        expr2 = self.compf2.expr.subs({x: mysymbols[instance_property[1]], a: 1, b: 0})
        return values[0] * expr1 * expr2 + values[1]

    def __str__(self):
        return self.name()


class Fit(object):
    def __init__(self, f: Expr, instance_prop: list[str], perr, mse, popt, dic, data):
        self.f = f
        self.instance_prop = instance_prop
        self.perr = perr
        self.popt = popt
        self.mse = mse
        self.dic = dic
        self.data = data
        self.chart = None  # Will be set later when generating the chart

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

    def name_simple(self):
        return print_simplify(self.f)

    def name_html(self):
        return c2p(mathml(self.f))

    def __str__(self):
        return f"{self.instance_prop} = {self.name_calc()} (MSE: {self.mse:.2f})"


def load_df(path: str) -> DataFrame:
    return pd.read_csv(path)


fitting_functions_single: list[ComplexityFunctionSingle] = [
    ComplexityFunctionSingle("Log.", "log(x)"),
    ComplexityFunctionSingle("Linear", "x"),
    ComplexityFunctionSingle("Log. Linear", "x * log(x)"),
    ComplexityFunctionSingle("Quadratic", "x ** 2"),
    ComplexityFunctionSingle("Cubic", "x ** 3"),
    ComplexityFunctionSingle("Exponential", "2 ** (0.000_001*x)"),
    # Exponential functions can overflow easily, try to compensate by using smaller exponents to fit
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


def print_simplify(original_expr: Expr) -> Expr:
    expr = simplify(original_expr)
    formatter = CustomStrPrinter().doprint
    expr_str = formatter(expr)
    # expr2 = sympify(expr_str)
    # expr2 = simplify(expr2)
    return expr_str


def fold_profiler_data_csv(path: str) -> any:
    with open(path) as csv_file:
        # Header contains instance name algorithm name, iteration number
        instance, algorithm, iteration = csv_file.readline().strip().split(',')
        if algorithm == BEST_ALGORITHM or iteration == BEST_ITERATION:
            print(f"Skipping file {path}")
            return None, None, []

        events = []
        for line in csv_file:
            clazz, method, enter, exit = line.strip().split(',')
            events.append({'clazz': clazz, 'method': method, 'when': int(enter), 'enter': True})
            events.append({'clazz': clazz, 'method': method, 'when': int(exit), 'enter': False})

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
        if 'when' in i:
            events.append({'clazz': i['clazz'], 'method': i['method'], 'when': i['when'], 'enter': i['enter']})
        else:
            events.append({'clazz': i['clazz'], 'method': i['method'], 'when': i['enter'], 'enter': True, 'l': i['exit']-i['enter']})
            events.append({'clazz': i['clazz'], 'method': i['method'], 'when': i['exit'], 'enter': False, 'l': i['exit']-i['enter']})

    # Sort by when, if when is equal, then first enter true than enter false, if both are equal, then length from greater to smallest if enter is true, from smallest to greatest if enter is false
    #events.sort(key=lambda x: (x['when'], 1 if x['enter'] else 0, -x.get('l', 0) if x['enter'] else x.get('l', 0)))
    events.sort(key=lambda x: (x['when']))
    return instance, algorithm, iteration, events


def fold_profiler_data(path: str) -> DataFrame:
    timestats = []

    files = os.listdir(path)
    with alive_bar(len(files), force_tty=True) as bar:
        bar.title = f"Loading data"
        for f in files:
            fullpath = join(path, f)
            if f.endswith(".csv"):
                instance, algorithm, iteration, events = fold_profiler_data_csv(fullpath)
            elif f.endswith(".json"):
                instance, algorithm, iteration, events = fold_profiler_data_json(fullpath)
            else:
                print(f"Skipping file {f}, not a CSV or JSON")
                continue

            events.sort(key=lambda x: x['when'])

            if not events:
                #print(f"No events returned by loader for file {f}, skipping")
                continue

            stack = []

            for i in events:
                name = f"{i['clazz']}{NEW_LINE}{i['method']}"
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
                    timestats.append((
                        instance,
                        iteration,
                        full_name,
                        parent,
                        child,
                        exec_time,
                        exclusive_time
                    ))
            bar()
    print("Initializing count dataframe...")
    df_forcount = pd.DataFrame(timestats, columns=['instance', 'iter', 'component', 'parent', 'child', 'time', 'time_exclusive'])
    print("Initializing times dataframe...")
    df_fortimes = df_forcount.copy()

    print("Preparing data... (step 1/8)")
    df_forcount.drop(columns=['time_exclusive'], inplace=True)
    print("Preparing data... (step 2/8)")
    df_forcount = df_forcount.groupby(['instance', 'component', 'parent', 'child', 'iter'], as_index=False).agg(
        {'time': 'count'})
    print("Preparing data... (step 3/8)")
    df_forcount.drop(columns=['iter'], inplace=True)
    print("Preparing data... (step 4/8)")
    df_forcount = df_forcount.groupby(['instance', 'component', 'parent', 'child'], as_index=False).mean()

    print("Preparing data... (step 5/8)")
    df_fortimes.drop(columns=['iter'], inplace=True)
    print("Preparing data... (step 6/8)")
    df_fortimes = df_fortimes.groupby(['instance', 'component', 'parent', 'child'], as_index=False).mean()

    print("Preparing data... (step 7/8)")
    df = df_fortimes.merge(df_forcount, on=['instance', 'component', 'parent', 'child'], suffixes=('', '_count'))

    print("Preparing data... (step 8/8)")
    df.sort_values(by=['instance', 'component'], inplace=True)
    return df


def prepare_df(df: DataFrame, timestats: DataFrame) -> DataFrame:
    exp_instances = timestats['instance'].unique()
    cloned = df[df['id'].isin(exp_instances)]
    cloned = cloned.sort_values(by=['id'], inplace=False)
    cloned = cloned.drop(['id'], axis=1)
    return cloned


def generate_f_chart_2d(xy: DataFrame, fit: Fit, component_name, property_source='time'):
    fig = px.line()
    x_series = xy[fit.instance_prop[0]]
    # Calculate evenly spaced x-y pairs for the estimation line
    x_min, x_max = x_series.min(), x_series.max()
    fit_x = np.linspace(x_min, x_max, 1000)
    fit_x_df = pd.DataFrame({fit.instance_prop[0]: fit_x})
    fit_y = estimate_y(fit.f, fit_x_df, fit.instance_prop)
    fig.add_scatter(x=xy[fit.instance_prop[0]], y=xy[property_source], mode="markers", name="Data",
                    marker=dict(color=real_color))
    fig.add_scatter(x=fit_x, y=fit_y, name=fit.name_simple(), mode="lines", line=dict(color=best_color))

    fig.update_layout(
        title=dict(
            text=rf"{component_name.replace(NEW_LINE, '::').replace('/', '→')}<br>{property_source} is Θ({fit.name_simple()})",
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
    fig.add_scatter3d(x=xy[fit.instance_prop[0]], y=xy[fit.instance_prop[1]], z=xy[property_source], mode="markers",
                      name="Real",
                      marker=dict(color=real_color, opacity=0.7, symbol="diamond-open"), hovertemplate=hovertemplate)
    fig.add_mesh3d(x=fit.data.x1, y=fit.data.x2, z=fit.data.y, name=fit.name_simple(),
                   hovertemplate=hovertemplate, opacity=0.7, showlegend=True)

    fig.update_layout(
        title=dict(
            text=rf"{component_name.replace('<br>', '::').replace('/', '→')}<br>{property_source} is Θ({fit.name_simple()})",
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


def generate_constant_chart(fit, component_name, property_source):
    fig = px.scatter_3d()
    fig.add_annotation(text=f"{component_name} {property_source} is constant with value {fit.popt[0]}", showarrow=False, font={"size":20})
    return fig

def generate_f_chart(data: DataFrame, fit: Fit, component_name, property_source='time'):
    if len(fit.instance_prop) == 0:
        return generate_constant_chart(fit, component_name, property_source)

    xy = data[data['component'] == component_name][[*fit.instance_prop, property_source]]
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
                                                 maxfev=10000, bounds=(0, np.inf))

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

            popt_f = []
            for v in popt:
                if abs(v) < MIN_VALUE:
                    v = 0
                popt_f.append(v)
            f = f.replace(instance_property, popt_f)
            f = expand(f)
            return Fit(f, instance_property, perr, mse, popt, dic, data)

    except Warning as warn:
        if warn.args[0] == 'overflow encountered in power':
            # Some functions such as exponential can easily overflow, this is expected, and the function will be discarded
            return None
        elif warn.args[0] == 'Covariance of the parameters could not be estimated':
            # Bad / failed fit
            return None
        elif "divide by zero encountered in log" in warn.args[0]:
            # ignore
            return None
        else:
            logging.warning(f"{f}: {warn}")
            return None
    except RuntimeError as err:
        logging.warning(f"{f}: {err}")
        return None


def calculate_fitting_funcs(data: DataFrame, component: str, feature: str, property_src: str, analyze_bar) -> list[Fit]:
    xy = data[data['component'] == component][[feature, property_src]]

    x, y = xy[feature], xy[property_src].to_numpy()

    fits = []
    for f in fitting_functions_single:
        fit = calculate_fitting_func(x, y, f, [feature])
        analyze_bar()
        if fit:
            fits.append(fit)

    return fits


def calculate_fitting_funcs2(data: DataFrame, component: str, feature1: str, feature2: str,
                             property_src: str, analyze_bar) -> list[Fit]:
    xy = data[data['component'] == component][[feature1, feature2, property_src]]

    if len(xy) < MIN_POINTS:
        print(
            f"Instance property pair ({feature1}, {feature2}) has less than {MIN_POINTS} points after grouping, skipping")
        return []

    x, y = xy[[feature1, feature2]].to_numpy(), xy[property_src].to_numpy()

    fits = []
    for f in fitting_functions_double:
        fit = calculate_fitting_func(x, y, f, [feature1, feature2])
        analyze_bar()
        if fit:
            fit.mse = fit.mse / DOUBLE_IMPROVEMENT_ACCEPT_TRESHOLD
            fits.append(fit)

    return fits


def find_best_instance_property(features: list[str], data: DataFrame, component: str, prop_src: str, analyze_bar) -> list[Fit]:
    fits_single: list[Fit] = []
    for feature in features:
        fits_single += calculate_fitting_funcs(data, component, feature, prop_src, analyze_bar)

    Fit.sort(fits_single)
    fits_double: list[Fit] = []
    for i in range(len(features)):
        for j in range(i + 1, len(features)):
            fits_double += calculate_fitting_funcs2(data, component, features[i], features[j], prop_src, analyze_bar)

    Fit.sort(fits_double)

    # Return best 3 of each type
    fits = fits_single[:5] + fits_double[:7]
    Fit.sort(fits)
    return fits


def try_analyze_all_property_sources(instance_features: list[str], data: DataFrame):
    treemap_labels = []
    failed_components = []

    unique_components = data['component'].unique()

    n_f = len(instance_features)
    n_features_funcs = n_f*(n_f - 1)//2 * len(fitting_functions_double) + n_f * len(fitting_functions_single)
    total = n_features_funcs * len(unique_components) * len(PROPERTY_SOURCES)
    with alive_bar(total, force_tty=True, enrich_print=False) as analyze_bar:
        for component_name in unique_components:
            complexities = {}
            for property_source in PROPERTY_SOURCES:
                # If all values are equal, they do not depend on any instance property, skip estimation
                uniq  = np.unique(data[data['component'] == component_name][property_source])
                if len(uniq) == 1:
                    complexities[property_source] = [Fit(sympify(uniq[0]), [], [0, 0], 0, [uniq[0], 0], {}, pd.DataFrame())]
                    analyze_bar(n_features_funcs)
                else:
                    complexities[property_source] = find_best_instance_property(instance_features, data, component_name,
                                                                            property_source, analyze_bar)
                # If all fits have failed, skip property
                if not complexities[property_source]:
                    failed_components.append(component_name)
                    print(f"Failed to calculate complexity of component {component_name}, skipping")
                    return None
                best = complexities[property_source][0]
                for i in range(len(complexities[property_source])):
                    current = complexities[property_source][i]
                    f_chart = generate_f_chart(data, current, component_name, property_source)
                    current.chart = f_chart
                    # Fits are sorted from better to worse, so if there is a collision, the previous fit is better
                    if current.name_simple() not in fits_by_id[component_name][property_source]:
                        fits_by_id[component_name][property_source][current.name_simple()] = current

                print(
                    f"{component_name.replace(NEW_LINE, '::')} ({property_source}) --> Θ({best.name_simple()}) - {Fit.get_metric_name()}: {best.get_metric_value():.2f}")
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
        c_combined_simpl = simplify(c_combined)

        simple = all_formulas[(all_formulas['component'] == component_name) & (all_formulas.property_source == "time")]
        simple_metric_v = simple[Fit.get_metric_name()].values[0]
        c_simple = simple.calc_function.values[0]
        c_simple_simpl = simplify(c_simple)

        simple_exclusive = all_formulas[
            (all_formulas['component'] == component_name) & (all_formulas.property_source == "time_exclusive")]
        simple_exclusive_metric_v = simple_exclusive[Fit.get_metric_name()].values[0]
        c_simple_exclusive = simple_exclusive.calc_function.values[0]
        c_simpl_simple_exclusive = simplify(c_simple_exclusive)

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
        tree_df.loc[tree_df['component'] == component_name, 'f_comb'] = print_simplify(c_combined_simpl)
        tree_df.loc[tree_df['component'] == component_name, 'f_simpl'] = print_simplify(c_simple_simpl)
        tree_df.loc[tree_df['component'] == component_name, 'f_simpl_excl'] = print_simplify(c_simpl_simple_exclusive)
        tree_df.loc[tree_df['component'] == component_name, Fit.get_metric_name()] = simple_metric_v
        tree_df.loc[tree_df['component'] == component_name, 'mse_comb'] = mse_comb
        tree_df.loc[tree_df['component'] == component_name, 'mse_simpl'] = mse_simpl
        tree_df.loc[tree_df['component'] == component_name, 'mse_simpl_excl'] = mse_simpl_excl

    return tree_df


def estimate_y(formula, xy: DataFrame, instance_features: list[str]) -> Series:
    def eval_formula(row):
        subs = {feature: row[feature] for feature in instance_features}
        return float(formula.evalf(subs=subs))

    y_estimated = xy.apply(eval_formula, axis=1)
    return y_estimated


def mse(formula, instance_features, data: DataFrame, component_name: str, property_source: str) -> float:
    xy = data[data['component'] == component_name][['instance', property_source, *instance_features]]
    xy['y_estimated'] = estimate_y(formula, xy, instance_features)

    mse = np.mean((xy[property_source] - xy.y_estimated) ** 2)
    return float(mse)  # np uses 'floating' type

def f_short(f):
    # f is a column in a dataframe, create a new column with f to string and if longer than 40 chars cut string
    return f.str.slice(0, 40)

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
            (
                treemap_data.time_total, treemap_data.time, treemap_data.time_count,
                treemap_data.time_total_excl, treemap_data.time_exclusive, treemap_data.time_count,
                treemap_data.mse_comb, f_short(treemap_data.f_comb),
                treemap_data.mse_simpl, f_short(treemap_data.f_simpl),
                treemap_data.mse_simpl_excl, f_short(treemap_data.f_simpl_excl),
            ), axis=-1),
        hovertemplate='''
<b>%{label} </b> <br>
Total T(ms): %{customdata[0]:.2f} (%{customdata[1]:.2f} * %{customdata[2]:.2f}) <br>
Exclusive T(ms): %{customdata[3]:.2f} (%{customdata[4]:.2f} * %{customdata[5]:.2f})<br>
Whitebox (MSE: %{customdata[6]:.1f}): %{customdata[7]} <br>
Blackbox (MSE: %{customdata[8]:.1f}): %{customdata[9]} <br>
Exclusive (MSE: %{customdata[10]:.1f}): %{customdata[11]}
<extra></extra>
        ''',
        marker=dict(
            colors=treemap_data.time_total_excl_scaled,
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

        maxdepth=MAX_DEPTH,
        legend="legend"
    ))

    heatmap_pane = pn.pane.Plotly(fig)
    selectA = pn.widgets.Select(name='Type')
    selectB = pn.widgets.Select(name='Ranked fits')
    detail_pane = pn.Accordion()
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
        update_scatter_plots(detail_pane, value)

    def update_selector_paneA(component_id):
        nonlocal last_selected_component, last_selected_type
        print("Selected component: ", component_id)
        last_selected_component = component_id
        new_options = {}
        for type in fits_by_id[last_selected_component].keys():
            new_options[f"{type}: {last_selected_component.replace(NEW_LINE, '::')}"] = type
        selectA.options = new_options
        selectA.value = next(iter(selectA.options.values()))
        update_selector_paneB(selectA.value)

    def update_selector_paneB(type_id):
        nonlocal last_selected_component, last_selected_type
        print("Selected type: ", type_id)
        last_selected_type = type_id
        selectB.options = list(fits_by_id[last_selected_component][last_selected_type].keys())
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
        v = fits_by_id[last_selected_component][last_selected_type][fit_id]
        scatter_pane.append((k, v.chart))
        scatter_pane.append(("Details", pn.pane.HTML(f"""
        <h1> Detailed data for {last_selected_component.replace(NEW_LINE, '::')} </h1>
        <p>  Full formula: {v.name_html()} </p>
        <p>  MSE: {v.get_metric_value()} </p>
        <p>  perr: {v.perr} </p>
        <p>  popt: {v.popt} </p>
        """)))
        scatter_pane.active = [0]

    control_pane = pn.Column(heatmap_pane, on_click_heatmap, selectA, on_selectA_change, selectB, on_selectB_change)
    update_selector_paneA(root_component_id)

    root_panel = pn.FlexBox(control_pane, detail_pane)
    return root_panel

def main():
    parser = argparse.ArgumentParser(
        description='Creates a set of instances to use during the experimentation',
        epilog='Created for the Mork project, if useful for your research consider citing the original publication')
    parser.add_argument('-p', '--properties', required=False, default="instance_properties.csv",
                        help="CSV Input file containing instance properties.")
    parser.add_argument('-i', '--data', required=False, default="solutions",
                        help="Path to folder which contains profiler data. Data can be stored in either CSV files or JSON, see docs for more details on the format.")

    # parser.add_argument('-dfi', '--dataframe-input', required=False, help="Path to cached dataframe")
    # parser.add_argument('-dfo', '--dataframe-output', required=False, help="Generate a cached dataframe")
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
