import argparse
import shutil
import os
import json

from collections import defaultdict
from statistics import mean

from os.path import join
import numpy as np

from scipy.optimize import curve_fit

import pandas as pd  # data manipulation and analysis
from pandas import DataFrame

import plotly.express as px
import plotly.graph_objects as go

boring_colors = ["#EDEDE9", "#D6CCC2", "#F5EBE0", "#E3D5CA", "#d6e2e9"]
real_color = "dodgerblue"
best_color = "limegreen"

def load_df(path: str) -> DataFrame:
    return pd.read_csv(path)


def prepare_df(df: DataFrame) -> DataFrame:
    df.sort_values(by=['id'], inplace=True)
    cloned = df.drop(["id"], axis=1)
    #cloned = df
    #cloned = pd.DataFrame(preprocessing.scale(cloned))
    return cloned


def get_functions() -> dict:
    return {
        r"a": lambda x, a: np.full(x.size, a),
        r"a \cdot \log(n)": lambda x, a: a * np.log(x),
        r"a \cdot n": lambda x, a: a * x,
        r"a \cdot n \log(n)": lambda x, a: a * x * np.log(x),
        r"a \cdot n^2": lambda x, a: a * x ** 2,
        r"a \cdot 2^n": lambda x, a: a * 2 ** x,
    }

def get_full_name(stack: list[tuple[str, int]]) -> str:
    return "/".join(name for name, _ in stack)

def fold_profiler_data(path: str) -> DataFrame:
    """
    List all json files in data folder and load them
    :param path:
    :return:
    """
    timestats = []

    for f in os.listdir(path):
        if not f.endswith(".json") or not "bestalg" in f or not "bestiter" in f:
            continue

        print("Processing", f)
        with open(join(path, f)) as json_file:
            data = []
            jsondata = json.load(json_file)

        for i in jsondata['timeData']:
            data.append({'when': i['when'], 'enter': i['enter'], 'clazz': i['clazz'], 'method': i['method']})
        data.sort(key=lambda x: x['when'])

        dict_data = defaultdict(list)
        stack = []

        for i in data:
            name = f"{i['clazz']}::{i['method']}"
            if i['enter']:
                stack.append((name, i['when']))
            else:
                current = get_full_name(stack)
                start = stack.pop()
                if name != start[0]:
                    raise Exception(f"Unexpected stack frame: {name} != {start[0]}")
                dict_data[current].append((i['when'] - start[1])/1000000) # Convert nanos to millis

        for k, v in dict_data.items():
            parent, child = k.rsplit("/", 1) if "/" in k else ("", k)
            timestats.append({"instance": jsondata['instanceId'], "component": k, "parent": parent, "child": child, "time": mean(v)})

    return pd.DataFrame(timestats).sort_values(by=['instance', 'component'])

def to_sorted_df(x, y) -> DataFrame:
    df = pd.DataFrame(dict(
        x=x,
        y=y
    ))
    df.sort_values(by=['x'], inplace=True)
    return df

def analyze_complexity(instances: DataFrame, timestats: DataFrame, param):
    treemap_labels = []
    for c in timestats['component'].unique():
        #if "Null" in c:
        #    continue
        best_per_instance = []
        for col in instances.columns:
            x = instances[col]
            max_x = max(x)
            y = np.array(timestats[timestats['component'] == c]['time'])
            # fig = px.line({"x": x, "y": y}, x=col, y="T (ms)", title=f'{c}')

            fig = px.line()
            _df = to_sorted_df(x, y)
            fig.add_scatter(x=_df.x, y=_df.y, name="Real", line=dict(color=real_color))
            #fig.add_annotation(x=max_x + .1, y=max(y), text="Real", showarrow=False, xanchor='left')


            fits = []
            for k, v in param.items():
                popt, pcov, dic, _, _ = curve_fit(v, x, y, full_output=True)
                y_estimated = v(x, *popt)
                # residual sum of squares
                ss_res = np.sum((y - y_estimated) ** 2)
                # total sum of squares
                ss_tot = np.sum((y - np.mean(y)) ** 2)
                # r-squared
                r2 = 1 - (ss_res / ss_tot)
                data = to_sorted_df(x,y_estimated)

                name = f"{k}".replace("a", str(round(popt[0], 1)))
                fits.append({"name": name, "instance_prop": col, "r2": r2, "popt": popt, "dic": dic, "data":data})


            fits.sort(key=lambda e: e['r2'], reverse=True)
            for i in range(len(fits)):
                e = fits[i]
                color = boring_colors[i % len(boring_colors)] if i != 0 else best_color
                fig.add_scatter(x=e['data'].x, y=e['data'].y, name=f"${e['name']}$", line=dict(color=color))
                # fig.add_annotation(x=max_x + 0.01, y=max(y_estimated), text=k, showarrow=False, xanchor='left')
                # print(f"Component {c} - Function {k} - {col} - R2: {r2} - {popt} - {dic['fvec']}")

            fig.update_layout(title=rf"$\text{{{c} is }}Θ({fits[0]['name']})$", showlegend=True, xaxis_title=col, yaxis_title="T (ms)")
            fig.show()
            best_per_instance.append(fits[0])

        best_per_instance.sort(key=lambda e: e['r2'], reverse=True)
        best = best_per_instance[0]
        print(f"Component {c} performance predicted as Θ({best['name']}) by {best['instance_prop']} - R2: {best['r2']}")
        treemap_labels.append({"component": c, "property": best['instance_prop'], "function": f"Θ({best['name']})", "r2": best['r2']})

    treemap_data = timestats.groupby(['component', 'parent', 'child'], as_index=False)['time'].mean()
    treemap_data = treemap_data.merge(pd.DataFrame(treemap_labels), on='component')

    fig = go.Figure()
    fig.add_trace(go.Treemap(
        ids=treemap_data.component,
        labels=treemap_data.child,
        parents=treemap_data.parent,
        customdata=np.stack((treemap_data.time, treemap_data.property, treemap_data.function.str.replace(r'\cdot','⋅'), treemap_data.r2), axis=-1),
        hovertemplate='<b> %{label} </b> <br> Time: %{customdata[0]:.2f} ms <br> Complexity: %{customdata[2]} <br> Where n is: %{customdata[1]} <br> R2: %{customdata[3]:.2f}',
        marker=dict(
            colors=treemap_data.time,
            colorscale='ylorbr',
            colorbar=dict(title='T (ms)'),
            cmid=treemap_data.time.mean(),
            showscale=True
        ),
        maxdepth=3,
        legend="legend"
    ))
    fig.update_layout(
        margin=dict(t=50, l=25, r=25, b=25),
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
    print(f"Preparing CSV data")
    instances = prepare_df(instances)
    print("Loading profiler data")
    timestats = fold_profiler_data(args.data)

    print(f"Analyzing complexity")
    analyze_complexity(instances, timestats, get_functions())

    print(f"All done, bye!")


if __name__ == '__main__':
    main()
