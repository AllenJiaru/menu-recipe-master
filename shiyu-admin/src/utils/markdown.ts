import { Marked } from 'marked'
import hljs from 'highlight.js'
import { markedHighlight } from 'marked-highlight'

const marked = new Marked(
  markedHighlight({
    langPrefix: 'hljs language-',
    highlight(code: string, lang: string) {
      if (lang && hljs.getLanguage(lang)) {
        try { return hljs.highlight(code, { language: lang }).value } catch {}
      }
      return hljs.highlightAuto(code).value
    }
  }),
  { breaks: true, gfm: true }
)

const sanitizeHtml = (html: string): string => {
  return html
    .replace(/href\s*=\s*["']?\s*javascript\s*:/gi, 'href="#"')
    .replace(/src\s*=\s*["']?\s*javascript\s*:/gi, 'src=""')
    .replace(/on\w+\s*=\s*["'][^"']*["']/gi, '')
    .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
    .replace(/<iframe\b[^<]*(?:(?!<\/iframe>)<[^<]*)*<\/iframe>/gi, '')
}

export const renderMarkdown = (text: string): string => {
  if (!text) return ''
  const sanitized = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
  const html = marked.parse(sanitized) as string
  return sanitizeHtml(html)
}
