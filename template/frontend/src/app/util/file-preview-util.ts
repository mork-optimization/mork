export class FilePreviewUtil {
  static canPreview(path: string): boolean {
    return /\.(log|jsonl|txt|json|csv|tsv|md|xml|ya?ml|properties|index|r|rout|out|err)$/i.test(path);
  }
}
