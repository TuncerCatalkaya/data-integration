interface Language {
    language: string
    country: string
}

const Languages: Record<string, Language> = {
    en: {
        language: "en",
        country: "gb"
    },
    de: {
        language: "de",
        country: "de"
    },
    pt: {
        language: "pt",
        country: "br"
    }
}

export default Languages
