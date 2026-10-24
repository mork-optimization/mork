import { InstanceLabelUtil } from './instance-label-util';

describe('InstanceLabelUtil', () => {
  it('removes the common directory while preserving full identifiers and distinct suffixes', () => {
    const paths = ['/home/ubuntu/campaign/data/00000000/instance.txt', '/home/ubuntu/campaign/data/00000001/instance.txt'];
    expect([...InstanceLabelUtil.relativeLabels(paths)]).toEqual([
      [paths[0], '00000000/instance.txt'], [paths[1], '00000001/instance.txt'],
    ]);
  });

  it('keeps filename prefixes and shortens a single path to its filename', () => {
    expect([...InstanceLabelUtil.relativeLabels(['/work/n250.001.txt', '/work/n250.002.txt']).values()]).toEqual(['n250.001.txt', 'n250.002.txt']);
    expect(InstanceLabelUtil.relativeLabels(['/work/n250.001.txt']).get('/work/n250.001.txt')).toBe('n250.001.txt');
  });

  it('normalizes Windows separators only in display labels', () => {
    const paths = ['C:\\runs\\set-a\\one.txt', 'C:\\runs\\set-b\\two.txt'];
    expect([...InstanceLabelUtil.relativeLabels(paths)]).toEqual([[paths[0], 'set-a/one.txt'], [paths[1], 'set-b/two.txt']]);
  });

  it('leaves unrelated paths intact and handles an empty selection', () => {
    expect([...InstanceLabelUtil.relativeLabels(['one.txt', 'two.txt']).values()]).toEqual(['one.txt', 'two.txt']);
    expect(InstanceLabelUtil.relativeLabels([]).size).toBe(0);
  });
});
