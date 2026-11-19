const STRAPI_URL = process.env.STRAPI_URL ?? "http://localhost:1337";

// Tipos para Categorías
export type Category = {
  id: number;
  documentId: string;
  name: string;
  slug: string;
};

// TODO 10: completar el tipo con url (string), alternativeText (string | null),
// width y height (number)
export type StrapiImage = {
  url: string;
  alternativeText: string | null;
  width: number;
  height: number;
};

// TODO 10: completar el tipo con id (number), documentId, name, slug,
// description (string), price y stock (number), image (StrapiImage | null) y category (Category | null)
export type Product = {
  id: number;
  documentId: string;
  name: string;
  slug: string;
  description: string;
  price: number;
  stock: number;
  image: StrapiImage | null;
  category: Category | null;
};

interface StrapiImagePayload {
  id: number;
  documentId?: string;
  name?: string;
  alternativeText?: string | null;
  caption?: string | null;
  width?: number | null;
  height?: number | null;
  url: string;
}

interface StrapiCategoryPayload {
  id: number;
  documentId?: string;
  name: string;
  slug: string;
}

interface StrapiProductPayload {
  id: number;
  documentId: string;
  name: string;
  slug: string;
  description?: string | null;
  price: number | string;
  stock?: number | null;
  image?: StrapiImagePayload | null;
  category?: StrapiCategoryPayload | null;
}

interface StrapiCollectionPayload {
  data: StrapiProductPayload[];
  meta?: {
    pagination?: {
      page: number;
      pageSize: number;
      pageCount: number;
      total: number;
    };
  };
}

function transformProduct(raw: StrapiProductPayload): Product {
  const image: StrapiImage | null =
    raw.image && typeof raw.image.url === "string"
      ? {
          url: raw.image.url,
          alternativeText: raw.image.alternativeText ?? null,
          width: typeof raw.image.width === "number" ? raw.image.width : 400,
          height: typeof raw.image.height === "number" ? raw.image.height : 300,
        }
      : null;

  const category: Category | null =
    raw.category && typeof raw.category.name === "string"
      ? {
          id: raw.category.id,
          documentId: raw.category.documentId ?? "",
          name: raw.category.name,
          slug: raw.category.slug,
        }
      : null;

  return {
    id: raw.id,
    documentId: raw.documentId,
    name: raw.name,
    slug: raw.slug,
    description: raw.description ?? "",
    price: typeof raw.price === "number" ? raw.price : Number(raw.price) || 0,
    stock: typeof raw.stock === "number" ? raw.stock : 10,
    image,
    category,
  };
}

export function getImageUrl(path: string): string {
  // TODO 11: Strapi devuelve rutas relativas como /uploads/foto.jpg.
  // Devolver STRAPI_URL + path si empieza con "/", o path tal cual si ya es absoluta.
  if (path.startsWith("http://") || path.startsWith("https://")) {
    return path;
  }
  const cleanBase = STRAPI_URL.replace(/\/+$/, "");
  const cleanPath = path.startsWith("/") ? path : `/${path}`;
  return `${cleanBase}${cleanPath}`;
}

export async function getProducts(categorySlug?: string): Promise<Product[]> {
  // TODO 12: fetch a `${STRAPI_URL}/api/products?populate=*`
  // con { cache: "no-store" }. Lanzar un Error si !res.ok.
  let url = `${STRAPI_URL}/api/products?populate=*`;
  if (categorySlug) {
    url += `&filters[category][slug][$eq]=${encodeURIComponent(categorySlug)}`;
  }

  const res = await fetch(url, {
    cache: "no-store",
  });

  if (!res.ok) {
    throw new Error(
      `Error al consultar productos desde Strapi (${res.status} ${res.statusText})`
    );
  }

  const json: StrapiCollectionPayload = await res.json();
  if (!json || !Array.isArray(json.data)) {
    throw new Error("Formato de respuesta inesperado recibido desde Strapi");
  }

  return json.data.map(transformProduct);
}

export async function getCategories(): Promise<Category[]> {
  const res = await fetch(`${STRAPI_URL}/api/categories`, {
    cache: "no-store",
  });

  if (!res.ok) {
    return [];
  }

  const json = await res.json();
  if (!json || !Array.isArray(json.data)) {
    return [];
  }

  return json.data.map((item: any) => ({
    id: item.id,
    documentId: item.documentId,
    name: item.name,
    slug: item.slug,
  }));
}

export async function getProductBySlug(slug: string): Promise<Product | null> {
  // TODO 13: fetch a `${STRAPI_URL}/api/products?filters[slug][$eq]=...&populate=*`
  const encodedSlug = encodeURIComponent(slug);
  const res = await fetch(
    `${STRAPI_URL}/api/products?filters[slug][$eq]=${encodedSlug}&populate=*`,
    {
      cache: "no-store",
    }
  );

  if (!res.ok) {
    throw new Error(
      `Error al consultar producto con slug '${slug}' (${res.status} ${res.statusText})`
    );
  }

  const json: StrapiCollectionPayload = await res.json();
  if (!json || !Array.isArray(json.data) || json.data.length === 0) {
    return null;
  }

  return transformProduct(json.data[0]);
}
