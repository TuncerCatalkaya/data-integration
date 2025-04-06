import { FuzzyAlgorithmType } from "./algorithm.types"

export default function DamerauLevenshteinDistance(source: string, target: string): FuzzyAlgorithmType {
    const distance = damerauLevenshteinDistance(source, target)
    const similarity = Math.max(0, 1 - distance / Math.max(source.length, target.length))
    return {
        distance,
        similarity
    }
}

function damerauLevenshteinDistance(source: string, target: string): number {
    const matrix: number[][] = []

    for (let s = 0; s <= source.length; s++) {
        matrix[s] = [s]
    }
    for (let t = 0; t <= target.length; t++) {
        matrix[0][t] = t
    }

    for (let s = 1; s <= source.length; s++) {
        for (let t = 1; t <= target.length; t++) {
            const cost = source[s - 1] === target[t - 1] ? 0 : 1

            matrix[s][t] = Math.min(
                matrix[s - 1][t] + 1, // Deletion
                matrix[s][t - 1] + 1, // Insertion
                matrix[s - 1][t - 1] + cost // Substitution
            )

            if (s > 1 && t > 1 && source[s - 1] === target[t - 2] && source[s - 2] === target[t - 1]) {
                matrix[s][t] = Math.min(matrix[s][t], matrix[s - 2][t - 2] + cost) // Transposition
            }
        }
    }

    return matrix[source.length][target.length]
}
