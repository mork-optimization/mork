MIN = 10
MAX = 5_000
STEP = 1.25
DENSITYS = [0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1]
OUTPUT = "instances"

import math
from networkx.generators.random_graphs import fast_gnp_random_graph
import networkx as nx
import random

from pathlib import Path

Path(OUTPUT).mkdir(parents=True, exist_ok=True)

current = MIN
while current < MAX:
    for d in DENSITYS:
        print(f"Generating graph {current}, d {d}...")
        graph_unweighted = fast_gnp_random_graph(current, d)
        # Ensure the graph is connected
        while not nx.is_connected(graph_unweighted):
            comps = list(nx.connected_components(graph_unweighted))
            graph_unweighted.add_edge(comps[0].pop(), comps[1].pop())
        graph_weighted = nx.Graph()
        for u, v in graph_unweighted.edges():
            graph_weighted.add_edge(u, v, weight=math.ceil(random.random()*100))

        with open(f"{OUTPUT}/graph_{current}_{d}.txt", "wb") as f:
            f.write(f"{len(graph_weighted.nodes)} {len(graph_weighted.edges)}\n".encode("utf-8"))
            nx.write_weighted_edgelist(graph_weighted, f)

    current = math.ceil(current*STEP)