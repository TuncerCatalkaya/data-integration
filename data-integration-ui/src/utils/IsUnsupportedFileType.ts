export default function IsUnsupportedFileType(file: File) {
    return !file.name.toLowerCase().endsWith(".csv")
}
