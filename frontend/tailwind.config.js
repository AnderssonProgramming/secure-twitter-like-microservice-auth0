/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        twitter: {
          blue:    '#1DA1F2',
          dark:    '#15202B',
          darker:  '#0D1117',
          border:  '#2F3336',
          muted:   '#8B98A5',
          white:   '#E7E9EA',
        }
      },
      fontFamily: {
        sans: ['"Chirp"', '"Segoe UI"', 'system-ui', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
