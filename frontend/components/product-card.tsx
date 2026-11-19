import Image from "next/image";
import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { getImageUrl, type Product } from "@/lib/strapi";
import { formatPrice } from "@/lib/utils";

export function ProductCard({ product }: { product: Product }) {
  const isOutOfStock = product.stock === 0;

  return (
    <Card className="overflow-hidden flex flex-col justify-between transition-shadow hover:shadow-lg">
      <div>
        {/* TODO 15a: si product.image existe, mostrar <Image> con src={getImageUrl(product.image.url)}, alt={product.image.alternativeText ?? product.name}, width={400}, height={300} y className="h-48 w-full object-cover" */}
        {product.image ? (
          <Image
            src={getImageUrl(product.image.url)}
            alt={product.image.alternativeText ?? product.name}
            width={400}
            height={300}
            className="h-48 w-full object-cover"
          />
        ) : (
          <div className="flex h-48 w-full items-center justify-center bg-muted text-muted-foreground">
            <span>Sin imagen disponible</span>
          </div>
        )}

        <CardHeader>
          {product.category && (
            <div className="mb-1">
              <Badge variant="outline" className="text-xs font-medium">
                {product.category.name}
              </Badge>
            </div>
          )}
          {/* TODO 15b: nombre del producto */}
          <CardTitle className="line-clamp-2 text-lg font-semibold">
            {product.name}
          </CardTitle>
        </CardHeader>

        <CardContent className="space-y-2">
          {/* TODO 15c: precio con formatPrice y un <Badge variant="destructive">Agotado</Badge> cuando product.stock sea 0 */}
          <div className="flex items-center justify-between">
            <span className="text-xl font-bold text-foreground">
              {formatPrice(product.price)}
            </span>
            {isOutOfStock ? (
              <Badge variant="destructive">Agotado</Badge>
            ) : (
              <span className="text-xs text-muted-foreground">
                Stock: {product.stock}
              </span>
            )}
          </div>
        </CardContent>
      </div>

      <CardFooter className="pt-2">
        {/* TODO 15d: <Button asChild> que envuelve un <Link> a `/productos/${product.slug}` con el texto "Ver detalle" */}
        <Button asChild className="w-full">
          <Link href={`/productos/${product.slug}`}>Ver detalle</Link>
        </Button>
      </CardFooter>
    </Card>
  );
}
