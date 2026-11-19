import Link from "next/link";
import { Button } from "@/components/ui/button";

export default function NotFound() {
  return (
    <main className="mx-auto flex min-h-[60vh] max-w-md flex-col items-center justify-center p-6 text-center">
      <div className="rounded-full bg-muted p-4 mb-4 text-3xl">🔍</div>
      <h1 className="text-4xl font-extrabold tracking-tight">404</h1>
      <h2 className="mt-2 text-xl font-semibold">Producto no encontrado</h2>
      <p className="mt-2 text-sm text-muted-foreground">
        El producto que estás buscando no existe o ha sido retirado del catálogo.
      </p>
      <Button asChild className="mt-6">
        <Link href="/">← Volver a la tienda</Link>
      </Button>
    </main>
  );
}
