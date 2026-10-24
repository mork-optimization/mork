export class InstanceLabelUtil {
  static relativeLabels(paths: readonly string[]): Map<string, string> {
    if (!paths.length) return new Map();
    let common = paths[0].replaceAll('\\', '/');
    for (const path of paths.slice(1)) {
      const normalized = path.replaceAll('\\', '/');
      let length = 0;
      while (length < common.length && common[length] === normalized[length]) length++;
      common = common.slice(0, length);
    }
    const prefixLength = common.lastIndexOf('/') + 1;
    const labels = new Map<string, string>();
    for (const path of paths) labels.set(path, path.replaceAll('\\', '/').slice(prefixLength));
    return labels;
  }
}
