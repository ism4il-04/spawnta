/** Renders API qrCode as an img src (PNG data URI or fallback QR image URL). */
export function qrCodeImageSrc(qrCode: string | null | undefined): string | null {
  if (!qrCode) {
    return null;
  }
  if (qrCode.startsWith('data:image')) {
    return qrCode;
  }
  return `https://api.qrserver.com/v1/create-qr-code/?size=280x280&data=${encodeURIComponent(qrCode)}`;
}
