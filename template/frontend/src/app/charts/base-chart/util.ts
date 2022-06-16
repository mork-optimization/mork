export class ChartUtil{

  getRandomColor(id: number) {
    id *= 3;
    return `rgb(${this.random(id)}, ${this.random(id + 1)}, ${this.random(id + 2)})`;
  }

  random(seed: number) {
    var x = Math.sin(seed) * 10000;
    return Math.floor((x - Math.floor(x)) * 256);
  }
}
