import { defineConfig } from "vite"
import react from "@vitejs/plugin-react"
import cssInjectedByJsPlugin from "vite-plugin-css-injected-by-js"

export default defineConfig({
    plugins: [react(), cssInjectedByJsPlugin()],
    build: {
        rollupOptions: {
            input: {
                index: "src/Index.tsx"
            },
            output: {
                dir: "dist",
                entryFileNames: "data-integration-ui-bundle.js",
                inlineDynamicImports: false,
                format: "iife",
                manualChunks: undefined
            }
        },
        minify: true
    }
})
