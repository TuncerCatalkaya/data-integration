import ContainCheck from "./ContainCheck"
import { FuzzyAlgorithmType } from "./algorithm.types"
import LevenshteinDistance from "./LevenshteinDistance"
import DamerauLevenshteinDistance from "./DamerauLevenshteinDistance"
import JaroSimilarity from "./JaroSimilarity"
import JaroWinklerSimilarity from "./JaroWinklerSimilarity"
import CosineSimilarity from "./CosineSimilarity"

interface Algorithm {
    display: string
    algorithm: (source: string, target: string) => FuzzyAlgorithmType
}

const Algorithms: Record<string, Algorithm> = {
    containCheck: {
        display: "Contain Check",
        algorithm: ContainCheck
    },
    levenshteinDistance: {
        display: "Levenshtein Distance",
        algorithm: LevenshteinDistance
    },
    damerauLevenshteinDistance: {
        display: "Damerau-Levenshtein Distance",
        algorithm: DamerauLevenshteinDistance
    },
    jaroSimilarity: {
        display: "Jaro Similarity",
        algorithm: JaroSimilarity
    },
    jaroWinklerSimilarity: {
        display: "Jaro-Winkler Similarity",
        algorithm: JaroWinklerSimilarity
    },
    cosineSimilarity: {
        display: "Cosine Similarity",
        algorithm: CosineSimilarity
    }
}

export default Algorithms
