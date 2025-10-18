import argparse
import shutil
import os
import json
import sys

from collections import defaultdict
from statistics import mean

from os.path impor
t join
import numpy as np

from scipy.optimize import curve_fit

import pandas as pd  # data manipulation and analysis
from pandas import DataFrame

import plotly.express as px
import plotly.graph_objects as go
from plotly.subplots import make_subplots

# Use colors from a plotly scale
colors = px.colors.qualitative.Plotly


def load_df(path: str) -> DataFrame:
    df = pd.read_csv(path, sep="\s+", names=["Distance", "Latency", "Profit", "nClients"])
    df.drop(['nClients'], axis=1, inplace=True)
    df['Profit'] = df['Profit'].abs()
    return df


def scatter_tests(df1: DataFrame, df2: DataFrame):
    fig = make_subplots(rows=2, cols=2)
    fig.add_trace(
        go.Scatter(x=df1['Profit'], y=df1['Latency'], mode='markers', name='SOTA'),
        row=1, col=1
    )
    fig.add_trace(
        go.Scatter(x=df2['Profit'], y=df2['Latency'], mode='markers', name='Mio'),
        row=1, col=1
    )

    fig.add_trace(
        go.Scatter(x=df1['Profit'], y=df1['Distance'], mode='markers', name='SOTA'),
        row=1, col=2
    )
    fig.add_trace(
        go.Scatter(x=df2['Profit'], y=df2['Distance'], mode='markers', name='Mio'),
        row=1, col=2
    )

    fig.add_trace(
        go.Scatter(x=df1['Distance'], y=df1['Latency'], mode='markers', name='SOTA'),
        row=2, col=1
    )
    fig.add_trace(
        go.Scatter(x=df2['Distance'], y=df2['Latency'], mode='markers', name='Mio'),
        row=2, col=1
    )

    #fig.update_layout(height=600, width=800, title_text="Side By Side Subplots")
    fig.show()


def pareto3d(fronts: list[DataFrame]):
    meshes = []

    for i, front in enumerate(fronts):
        meshes.append(go.Mesh3d(
            x=front['Distance'],
            y=front['Latency'],
            z=front['Profit'],
            opacity=0.5,
            color=colors[i]
        ))

    fig = go.Figure(data=meshes)
    fig.update_layout(
        scene=dict(
            xaxis_title='Distance',
            yaxis_title='Latency',
            zaxis_title='Profit'
            #xaxis=dict(nticks=4, range=[-100, 100], ),
            #yaxis=dict(nticks=4, range=[-50, 100], ),
            #zaxis=dict(nticks=4, range=[-100, 100], ),
        )
        #width=700,
        #margin=dict(r=20, l=10, b=10, t=10)
    )

    fig.show()

def main():
    # parser = argparse.ArgumentParser(
    #     description='Creates a set of instances to use during the experimentation',
    #     epilog='Created for the Mork project, if useful for your research consider citing the original publication')
    # parser.add_argument('-p', '--properties', required=False, default="instance_properties.csv", help="CSV Input file containing instance properties.")
    # parser.add_argument('-i', '--data', required=False, default="solutions", help="Path to folder which contains profiler data")
    # parser.add_argument('-o', '--output', required=False, default="output", help="Path to output folder.")

    # args = parser.parse_args()

    #shutil.rmtree(args.output, ignore_errors=True)
    #os.mkdir(args.output)
    if len(sys.argv) <= 2:
        print("Usage: python pareto3d.py <pareto1> <pareto2> ...")
        return

    frames = []
    for arg in sys.argv:
        if arg == sys.argv[0]:
            continue
        print(f"Loading front {arg}")
        df = load_df(arg)
        frames.append(df)

    #pareto3d(frames)
    scatter_tests(frames[0], frames[1])

    print(f"All done, bye!")


if __name__ == '__main__':
    main()
