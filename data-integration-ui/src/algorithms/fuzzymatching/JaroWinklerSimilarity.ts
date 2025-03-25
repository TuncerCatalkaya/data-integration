import { FuzzyAlgorithmType } from "./algorithm.types"
import JaroSimilarity from "./JaroSimilarity"

export default function JaroWinklerSimilarity(source: string, target: string): FuzzyAlgorithmType {
    const similarity = jaroWinklerSimilarity(source, target)
    const distance = 1 - similarity
    return {
        distance,
        similarity
    }
}

function jaroWinklerSimilarity(source: string, target: string): number {
    const similarity = JaroSimilarity(source, target).similarity

    const prefixLength = getPrefixLength(source, target)
    const scaling = 0.1

    return similarity + prefixLength * scaling * (1 - similarity)
}

function getPrefixLength(source: string, target: string): number {
    let prefixLength = 0
    const maxPrefixLength = 4

    for (let i = 0; i < Math.min(source.length, target.length, maxPrefixLength); i++) {
        if (source[i] === target[i]) {
            prefixLength++
        } else {
            break
        }
    }

    return prefixLength
}
