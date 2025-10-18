MIN = 10
MAX = 5_000
STEP = 1.25
DENSITYS = [0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1]
NSEEDS = 10
OUTPUT = "instances"

import math
import random

from pathlib import Path

Path(OUTPUT).mkdir(parents=True, exist_ok=True)

current = MIN
while current < MAX:
    for d in DENSITYS:
        for i in range(NSEEDS):
            seed = random.randint(0, 1_000_000_000)
            print(f"Generating graph {current}, d {d}, seed {seed}...")
            with open(f"{OUTPUT}/graph_{current}_{d}_{seed}.gen", "w") as f:
                f.write(f"{current} {d} {seed}\n")

    current = math.ceil(current*STEP)