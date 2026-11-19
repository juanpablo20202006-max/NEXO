export { cn } from "cn";

// TODO 14: Formatear precios en pesos colombianos (COP) de manera consistente
export function formatPrice(value: number): string {
  return new Intl.NumberFormat("es-CO", {
    style: "currency",
    currency: "COP",
    maximumFractionDigits: 0,
  }).format(value);
}
