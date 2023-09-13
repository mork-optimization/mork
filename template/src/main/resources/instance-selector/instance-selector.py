import argparse
import shutil
import os

import math  # math
from os.path import join

import pandas as pd  # data manipulation and analysis
import numpy as np  # working with arrays and matrices

from pandas import DataFrame
from sklearn import preprocessing  # standardization  of data
from sklearn.decomposition import PCA  # principal component analysis
from sklearn.cluster import KMeans  # K-Means clustering
from sklearn.metrics import pairwise_distances  # Compute the distance matrix between two vectors
from yellowbrick.cluster import KElbowVisualizer, \
    SilhouetteVisualizer  # implements the “elbow” method to help data scientists select the optimal number of clusters by fitting the model with a range of values for K

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt  # plotting library


import seaborn as sns  # plotting library

import warnings
from matplotlib import MatplotlibDeprecationWarning
warnings.filterwarnings("ignore",category=MatplotlibDeprecationWarning)
warnings.filterwarnings("ignore",category=FutureWarning)


DEFAULT_COLORS = ['red', 'green', 'blue', 'orange', 'purple', 'cyan', 'brown', 'olive', 'gray', 'olive', 'crimson',
                  'teal']


def load_df(path: str) -> DataFrame:
    return pd.read_csv(path)


def prepare_df(df: DataFrame) -> DataFrame:
    cloned = df.drop(["id"], axis=1)
    preprocessing.scale(cloned, copy=False)
    return cloned


def save_correlation(df: DataFrame, output_path: str):
    correlation = df.corr()
    fig = plt.figure(figsize=(10, 10))
    sns.heatmap(correlation, vmax=1, square=True, annot=True, cmap='cubehelix')
    plt.title('Correlation between different features')
    plt.savefig(join(output_path, "correlation.pdf"))
    plt.close(fig)
    plt.clf()


def save_pca(df: DataFrame, output_path: str, explained_pca_ratio=0.90) -> DataFrame:
    pca = PCA().fit(df)
    line_data = np.cumsum(pca.explained_variance_ratio_)
    line_data = np.insert(line_data, 0, 0)
    plt.bar(np.arange(1, len(pca.explained_variance_ratio_) + 1), pca.explained_variance_ratio_, color='g')
    plt.plot(np.arange(0, len(line_data)), line_data, marker='D')
    plt.xlim(0, len(pca.explained_variance_ratio_), 1)
    plt.axhline(y=explained_pca_ratio, color='black', linestyle='--')
    plt.xlabel('Number of components')
    plt.ylabel('Cumulative explained variance')
    plt.savefig(join(output_path, "pca.pdf"))
    plt.clf()

    sklearn_pca = PCA(n_components=explained_pca_ratio)
    transformed_data = sklearn_pca.fit_transform(df)
    return transformed_data


def elbow_method(df: DataFrame, output_path: str, init_kmeans='k-means++', random_state=42, max_iter=1000,
                 max_n_cluster=15) -> (KMeans, int):
    model = KMeans(init=init_kmeans, random_state=random_state, max_iter=max_iter)

    visualizer = KElbowVisualizer(model, k=(2, max_n_cluster), timings=False)
    visualizer.fit(df)  # Fit the data to the visualizer
    visualizer.finalize()
    visualizer.ax.set_title("")
    visualizer.ax.set_ylabel("Distortion Score")
    visualizer.ax.set_xlabel("Number of clusters")
    visualizer.show(outpath=join(output_path, "kElbow.pdf"))
    plt.clf()

    n_clusters = visualizer.elbow_value_
    kmeans = KMeans(n_clusters=n_clusters, init=init_kmeans, random_state=random_state, max_iter=max_iter).fit(df)
    return kmeans, n_clusters


def save_silhouette(df: DataFrame, output_path: str, kmeans: KMeans):
    visualizer = SilhouetteVisualizer(kmeans, colors='yellowbrick')
    visualizer.fit(df)
    visualizer.finalize()
    visualizer.ax.set_title("")
    visualizer.ax.set_ylabel("Cluster ID")
    visualizer.ax.set_xlabel("Silhouette Coefficients")
    visualizer.show(outpath=join(output_path, "silhouette.pdf"))
    plt.clf()

def save_pairplot(df: DataFrame, output_path: str, labels, colors=DEFAULT_COLORS):
    color_dict = dict()
    for index, value in enumerate(colors):
        color_dict[index] = value

    result = {"Cluster Id": labels}
    for i in range(df.shape[1]):
        result["PCA  " + str(i)] = df[:, i]

    testdf = pd.DataFrame(result)
    pplot = sns.pairplot(testdf, hue="Cluster Id", palette=color_dict)
    pplot.savefig(join(output_path, "pairPlot.pdf"))
    plt.clf()


def select_instances(original_df: DataFrame, filtered_df: DataFrame, kmeans: KMeans, labels, n_cluster, size=0.15):
    distances = pairwise_distances(kmeans.cluster_centers_, filtered_df)
    distancesToCentroid = []
    i = 0
    for c in labels:
        distancesToCentroid.append(distances[c][i])
        i += 1

    clusters = {}
    clusters["ClusterId"] = labels
    clusters["Instance"] = [name for name in original_df["id"]]
    clusters["Distances"] = distancesToCentroid
    cluster_df = pd.DataFrame(clusters)

    sorted_df = []

    for k in range(n_cluster):
        dfk = cluster_df.loc[cluster_df["ClusterId"] == k]
        sorted_df.append(dfk.sort_values(by="Distances", ascending=True).to_numpy())

    # Sort by cluster size
    sorted_df.sort(key=lambda x: -len(x))

    instance_remaining = math.ceil(size * len(labels))
    preliminary_instances = []
    aux = [i for i in sorted_df.copy()]

    stopAt = instance_remaining
    takeFromCluster = 0
    while takeFromCluster < stopAt:
        cluster = aux[takeFromCluster % len(aux)]
        if len(cluster) == 0:
            stopAt += 1
        else:
            instance = cluster[0][1]
            preliminary_instances.append(instance)
            aux[takeFromCluster % len(aux)] = np.delete(cluster, 0, 0)

        takeFromCluster += 1

    return preliminary_instances


def cp_instances(chosen_instances: list[str], instance_folder: str, output_folder: str):
    preliminary_instance_path = join(output_folder, "instances")
    os.mkdir(preliminary_instance_path)
    for i in chosen_instances:
        src = join(instance_folder, i)
        dst = join(output_folder, "instances", i)
        shutil.copyfile(src, dst)

    print('Chosen instances have been copied to ', preliminary_instance_path)


def main():
    parser = argparse.ArgumentParser(
        description='Creates a set of instances to use during the experimentation',
        epilog='Created by Raul Martin, for the DRFLP project, if useful for your research consider citing the original work: https://doi.org/10.1162/evco_a_00317')
    parser.add_argument('-p', '--properties', required=False, default="instance_properties.csv", help="CSV Input file containing instance properties.")
    parser.add_argument('-i', '--instances', required=False, default="instances", help="Path to instance folder.")
    parser.add_argument('-o', '--output', required=False, default="output", help="Path to output folder.")
    parser.add_argument('-s', '--size', required=False, default=0.15,
                        help="Relative size of preliminary set. 0.15 means that 15% of instances will be selected.")
    parser.add_argument('-vr', '--varianceratio', required=False, default=0.90, help="Explained variance ratio.")

    args = parser.parse_args()

    shutil.rmtree(args.output, ignore_errors=True)
    os.mkdir(args.output)
    print(f"Loading CSV {args.properties}")
    original_df = load_df(args.properties)
    print(f"Preparing data")
    filtered_df = prepare_df(original_df)
    print(f"Calculating correlation")
    save_correlation(filtered_df, args.output)
    print(f"Running Principal Component Analysis")
    pca_df = save_pca(filtered_df, args.output, args.varianceratio)
    print(f"Number of properties reduced from {len(original_df.columns)} to {pca_df.shape[1]}")
    print(f"Running kmeans with elbow method")
    kmeans, n_clusters = elbow_method(pca_df, args.output)
    print(f"Classifying instances in {n_clusters} clusters")
    labels = kmeans.predict(pca_df)
    print(f"Generating silhouette diagram")
    save_silhouette(pca_df, args.output, kmeans)
    print(f"Generating pairplots")
    save_pairplot(pca_df, args.output, labels)
    print(f"Selecting instances according to centroid distance")
    size = float(args.size)
    chosen_instances = select_instances(original_df, pca_df, kmeans, labels, n_clusters, size)
    n_preliminar_instances = math.ceil(size * len(labels))
    print(f"""
        SUMMARY:
         - Instances analyzed: {len(labels)}
         - Preliminary %: {size}
         - Number of preliminary instances: {n_preliminar_instances}
         - Properties used: {list(filtered_df.columns)}
         - Number of clusters: {n_clusters}
         - Selected instances: {chosen_instances}
        """)
    cp_instances(chosen_instances, args.instances, args.output)
    print(f"All done, bye!")


if __name__ == '__main__':
    main()
