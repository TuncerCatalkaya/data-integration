import { FuzzyAlgorithmType } from "./algorithm.types"

export default function JaroSimilarity(source: string, target: string): FuzzyAlgorithmType {
    const similarity = jaroSimilarity(source, target)
    const distance = 1 - similarity
    return {
        distance,
        similarity
    }
}

function jaroSimilarity(source: string, target: string): number {
    if (source.length === 0 && target.length === 0) {
        return 1
    }

    const matchDistance = Math.floor(Math.max(source.length, target.length) / 2) - 1

    const sourceMatches = Array(source.length).fill(false)
    const targetMatches = Array(target.length).fill(false)
    let matches = 0

    for (let s = 0; s < source.length; s++) {
        for (let t = Math.max(0, s - matchDistance); t < Math.min(target.length, s + matchDistance + 1); t++) {
            if (source[s] === target[t] && !targetMatches[t]) {
                sourceMatches[s] = true
                targetMatches[t] = true
                matches++
                break
            }
        }
    }

    if (matches === 0) {
        return 0
    }

    let targetTranspositions = 0
    let t = 0

    for (let s = 0; s < source.length; s++) {
        if (sourceMatches[s]) {
            while (!targetMatches[t]) {
                t++
            }
            if (source[s] !== target[t]) {
                targetTranspositions++
            }
            t++
        }
    }

    targetTranspositions /= 2

    return (1 / 3) * (matches / source.length + matches / target.length + (matches - targetTranspositions) / matches)
}
