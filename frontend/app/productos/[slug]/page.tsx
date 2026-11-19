import Image from "next/image";
import Link from "next/link";
import { notFound } from "next/navigation";
import { formatPrice } from "@/lib/api";
import { ArrowLeft, ShoppingBag, ShieldCheck, Truck, RefreshCw } from "lucide-react";

export const dynamic = "force-dynamic";

interface ProductPageProps {
  params: Promise<{ slug: string }>;
}

async function getProductoBySlug(slug: string) {
  try {
    const res = await fetch(`http://localhost:8080/api/productos/slug/${slug}`, {
      cache: "no-store",
    });
    if (!res.ok) return null;
    return await res.json();
  } catch {
    return null;
  }
}

export default async function ProductDetailPage({ params }: ProductPageProps) {
  const { slug } = await params;
  const product = await getProductoBySlug(slug);

  if (!product) {
    notFound();
  }

  const isOutOfStock = product.stock <= 0;

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-6xl mx-auto">
        <Link
          href="/"
          className="inline-flex items-center gap-2 text-xs font-mono text-slate-400 hover:text-amber-400 mb-8 transition"
        >
          <ArrowLeft className="w-4 h-4" />
          Volver al Catálogo NEXO
        </Link>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-10 items-start">
          {/* Imagen de Alta Resolución */}
          <div className="relative aspect-[4/5] w-full bg-slate-900 rounded-3xl overflow-hidden border border-slate-800 shadow-2xl">
            <Image
              src={product.imagenUrl || "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=800&q=80"}
              alt={product.nombre}
              fill
              className="object-cover"
              priority
            />
            {product.destacado && (
              <span className="absolute top-4 left-4 bg-amber-500 text-slate-950 text-xs font-black px-3 py-1 rounded-full uppercase tracking-wider shadow">
                COLECCIÓN OFICIAL 2026
              </span>
            )}
          </div>

          {/* Información Detallada */}
          <div className="space-y-6">
            <div>
              <span className="text-xs font-mono text-amber-400 uppercase tracking-widest font-semibold">
                {product.categoria?.nombre || "STREETWEAR"}
              </span>
              <h1 className="text-3xl sm:text-4xl font-black text-white tracking-tight mt-1">
                {product.nombre}
              </h1>
              <p className="text-2xl font-black text-white mt-3 font-mono">
                {formatPrice(product.precio)}{" "}
                <span className="text-xs font-normal text-slate-400">COP (IVA incluido)</span>
              </p>
            </div>

            <div className="border-t border-b border-slate-800/80 py-4">
              <h2 className="text-xs font-mono uppercase text-slate-400 mb-2">Descripción de la prenda</h2>
              <p className="text-sm text-slate-300 leading-relaxed">
                {product.descripcion}
              </p>
            </div>

            {/* Disponibilidad */}
            <div className="flex items-center gap-3">
              <span className="text-xs font-mono text-slate-400">Disponibilidad:</span>
              {isOutOfStock ? (
                <span className="px-3 py-1 bg-red-950/60 border border-red-800/60 text-red-400 text-xs font-bold rounded-full">
                  Agotado
                </span>
              ) : (
                <span className="px-3 py-1 bg-emerald-950/60 border border-emerald-800/60 text-emerald-400 text-xs font-bold rounded-full">
                  En Stock ({product.stock} unidades disponibles)
                </span>
              )}
            </div>

            {/* Tallas disponibles */}
            <div>
              <span className="text-xs font-mono text-slate-400 block mb-2">Tallas en producción:</span>
              <div className="flex gap-2">
                {["S", "M", "L", "XL"].map((t) => (
                  <span
                    key={t}
                    className="w-9 h-9 rounded-xl bg-slate-900 border border-slate-700 flex items-center justify-center font-bold text-xs text-white"
                  >
                    {t}
                  </span>
                ))}
              </div>
            </div>

            {/* Botón de Regreso a Comprar */}
            <div className="pt-4">
              <Link
                href="/#catalogo"
                className="w-full py-3.5 bg-amber-500 hover:bg-amber-400 text-slate-950 rounded-xl font-bold text-xs uppercase tracking-wider transition flex items-center justify-center gap-2 shadow-lg shadow-amber-500/25"
              >
                <ShoppingBag className="w-4 h-4" />
                Comprar en la Tienda Principal
              </Link>
            </div>

            {/* Garantías y Respaldo */}
            <div className="grid grid-cols-3 gap-3 pt-4 border-t border-slate-800 text-center text-xs text-slate-400">
              <div className="p-3 bg-slate-900/60 rounded-xl border border-slate-800/60">
                <Truck className="w-5 h-5 mx-auto text-amber-400 mb-1" />
                <span>Despacho a todo el país</span>
              </div>
              <div className="p-3 bg-slate-900/60 rounded-xl border border-slate-800/60">
                <ShieldCheck className="w-5 h-5 mx-auto text-blue-400 mb-1" />
                <span>Pago contra entrega seguro</span>
              </div>
              <div className="p-3 bg-slate-900/60 rounded-xl border border-slate-800/60">
                <RefreshCw className="w-5 h-5 mx-auto text-emerald-400 mb-1" />
                <span>Cambios sin costo</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
