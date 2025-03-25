import { FuzzyAlgorithmType } from "./algorithm.types"

export default function ContainCheck(source: string, target: string): FuzzyAlgorithmType {
    const contains = source.includes(target) || target.includes(source)
    const distance = contains ? 0 : Math.max(source.length, target.length)
    const similarity = contains ? 1 : 0
    return {
        distance,
        similarity
    }
}
