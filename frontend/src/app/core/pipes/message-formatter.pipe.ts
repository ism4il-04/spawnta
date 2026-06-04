import { Pipe, PipeTransform, inject } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Pipe({
  name: 'messageFormatter',
  standalone: true
})
export class MessageFormatterPipe implements PipeTransform {
  private readonly sanitizer = inject(DomSanitizer);

  transform(value: string | null): SafeHtml {
    if (!value) return '';

    // 1. Escaping done in backend, so HTML tags are already safe string (e.g. &lt;script&gt;).
    // Let's do markdown replacement
    let formatted = value;

    // Bold replacement (**text** or __text__)
    formatted = formatted.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
    formatted = formatted.replace(/__(.*?)__/g, '<strong>$1</strong>');

    // Italic replacement (*text* or _text_)
    formatted = formatted.replace(/\*(.*?)\*/g, '<em>$1</em>');
    formatted = formatted.replace(/_(.*?)_/g, '<em>$1</em>');

    // Auto-link URLs (Requirement 8.4)
    const urlRegex = /(https?:\/\/[^\s]+)/g;
    formatted = formatted.replace(urlRegex, (url) => {
      // Escape target double quotes if any
      const escapedUrl = url.replace(/"/g, '&quot;');
      return `<a href="${escapedUrl}" target="_blank" class="chat-link" style="color: #0f766e; text-decoration: underline; font-weight: 600;">${url}</a>`;
    });

    // Bypass Angular security to render raw sanitized HTML safely
    return this.sanitizer.bypassSecurityTrustHtml(formatted);
  }
}
