import { FuzzyAlgorithmType } from "./algorithm.types"

export default function CosineSimilarity(source: string, target: string): FuzzyAlgorithmType {
    const sourceChars = [...source]
    const targetChars = [...target]

    const sourceVector = getCharFrequency(sourceChars)
    const targetVector = getCharFrequency(targetChars)

    const similarity = calculateCosineSimilarity(sourceVector, targetVector)
    const distance = 1 - similarity
    return {
        distance,
        similarity
    }
}

function getCharFrequency(chars: string[]): Map<string, number> {
    const frequencies = new Map<string, number>()
    chars.forEach(char => frequencies.set(char, (frequencies.get(char) ?? 0) + 1))
    return frequencies
}

function calculateCosineSimilarity(sourceVector: Map<string, number>, targetVector: Map<string, number>): number {
    let dotProduct = 0
    sourceVector.forEach((sourceValue, char) => {
        const targetValue = targetVector.get(char)!
        dotProduct += sourceValue * targetValue
    })

    const sourceMagnitude = Math.sqrt(Array.from(sourceVector.values()).reduce((sum, value) => sum + value * value, 0))
    const targetMagnitude = Math.sqrt(Array.from(targetVector.values()).reduce((sum, value) => sum + value * value, 0))

    return dotProduct / (sourceMagnitude * targetMagnitude)
}
