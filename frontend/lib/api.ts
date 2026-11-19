export interface Categoria {
  id: number;
  nombre: string;
  slug: string;
  descripcion?: string;
  activa: boolean;
}

export interface Producto {
  id: number;
  nombre: string;
  slug: string;
  descripcion: string;
  precio: number;
  stock: number;
  imagenUrl: string;
  tallas: string;
  destacado: boolean;
  categoria: Categoria;
}

export interface TiendaFisica {
  id: string;
  title: string;
  address: string;
  city: string;
  phone: string;
  rating: number;
  reviews: number;
  openState: string;
  thumbnail: string;
  latitude: number;
  longitude: number;
  placeId: string;
  nexoRegistrado: boolean;
}

export interface UserSession {
  authenticated: boolean;
  message?: string;
  id?: number;
  username?: string;
  rol?: "ADMIN" | "VENDEDOR" | "CLIENTE";
  token?: string;
}

export interface CartItem {
  producto: Producto;
  cantidad: number;
  talla: string;
}

const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";

export function formatPrice(value: number | string): string {
  const numeric = typeof value === "string" ? parseFloat(value) : value;
  if (isNaN(numeric)) return "$ 0";
  return new Intl.NumberFormat("es-CO", {
    style: "currency",
    currency: "COP",
    maximumFractionDigits: 0,
  }).format(numeric);
}

export async function getProductos(categoria?: string, search?: string): Promise<Producto[]> {
  try {
    let url = `${API_BASE}/productos`;
    const params = new URLSearchParams();
    if (categoria && categoria !== "todo") params.append("categoria", categoria);
    if (search) params.append("search", search);
    
    const queryString = params.toString();
    if (queryString) url += `?${queryString}`;

    const res = await fetch(url, { cache: "no-store" });
    if (!res.ok) throw new Error("Error consultando productos de NEXO");
    return await res.json();
  } catch (error) {
    console.error("Fallo al conectar con el backend de Spring Boot:", error);
    return [];
  }
}

export async function getCategorias(): Promise<Categoria[]> {
  try {
    const res = await fetch(`${API_BASE}/categorias`, { cache: "no-store" });
    if (!res.ok) throw new Error("Error consultando categorías");
    return await res.json();
  } catch (error) {
    console.error("Fallo consultando categorías:", error);
    return [];
  }
}

export async function getTiendasCercanas(query = "tiendas de ropa", location = "Villavicencio, Meta"): Promise<TiendaFisica[]> {
  try {
    const url = `${API_BASE}/serpapi/tiendas?query=${encodeURIComponent(query)}&location=${encodeURIComponent(location)}`;
    const res = await fetch(url, { cache: "no-store" });
    if (!res.ok) throw new Error("Error consultando tiendas de SerpAPI");
    return await res.json();
  } catch (error) {
    console.error("Fallo consultando SerpAPI:", error);
    return [];
  }
}

export async function loginUser(username: string, password: string): Promise<UserSession> {
  const res = await fetch(`${API_BASE}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: "Credenciales inválidas" }));
    throw new Error(err.message || "Error al autenticar");
  }
  return await res.json();
}

export async function submitOrder(orderData: {
  username: string;
  direccionEntrega: string;
  ciudad: string;
  telefono: string;
  items: { productoId: number; cantidad: number; talla: string }[];
}): Promise<any> {
  const res = await fetch(`${API_BASE}/pedidos`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(orderData),
  });
  if (!res.ok) {
    const errText = await res.text();
    throw new Error(errText || "Error al procesar el pedido");
  }
  return await res.json();
}

export async function getPedidos(): Promise<any[]> {
  try {
    const res = await fetch(`${API_BASE}/pedidos`, { cache: "no-store" });
    if (!res.ok) return [];
    return await res.json();
  } catch {
    return [];
  }
}
