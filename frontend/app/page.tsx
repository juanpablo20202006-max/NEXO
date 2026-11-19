"use client";

import React, { useState, useEffect } from "react";
import Image from "next/image";
import {
  ShoppingBag,
  MapPin,
  Search,
  Star,
  CheckCircle,
  Shield,
  User,
  LogOut,
  Plus,
  Minus,
  Trash2,
  ExternalLink,
  Phone,
  Clock,
  Sparkles,
  Layers,
  ArrowRight,
  Package,
  Store,
  ChevronRight,
  AlertCircle,
  Tag as TagIcon
} from "lucide-react";

import {
  Producto,
  Categoria,
  TiendaFisica,
  CartItem,
  UserSession,
  formatPrice,
  getProductos,
  getCategorias,
  getTiendasCercanas,
  loginUser,
  submitOrder,
  getPedidos
} from "@/lib/api";

export default function NexoStorefront() {
  // Estados de datos
  const [productos, setProductos] = useState<Producto[]>([]);
  const [categorias, setCategorias] = useState<Categoria[]>([]);
  const [tiendas, setTiendas] = useState<TiendaFisica[]>([]);
  const [pedidos, setPedidos] = useState<any[]>([]);

  // Filtros y búsqueda
  const [categoriaActiva, setCategoriaActiva] = useState<string>("todo");
  const [busquedaTexto, setBusquedaTexto] = useState<string>("");
  const [tallaSeleccionada, setTallaSeleccionada] = useState<{ [key: number]: string }>({});

  // SerpAPI Geolocalización
  const [busquedaTienda, setBusquedaTienda] = useState<string>("tiendas de ropa");
  const [ubicacionTienda, setUbicacionTienda] = useState<string>("Villavicencio, Meta");
  const [cargandoTiendas, setCargandoTiendas] = useState<boolean>(false);

  // Carrito de compras
  const [carrito, setCarrito] = useState<CartItem[]>([]);
  const [carritoAbierto, setCarritoAbierto] = useState<boolean>(false);
  const [pedidoExitoso, setPedidoExitoso] = useState<any | null>(null);

  // Formulario de Checkout
  const [direccion, setDireccion] = useState<string>("Calle 15 # 30-40, Barrio Barzal");
  const [ciudad, setCiudad] = useState<string>("Villavicencio");
  const [telefono, setTelefono] = useState<string>("310 987 6543");
  const [procesandoCompra, setProcesandoCompra] = useState<boolean>(false);

  // Sesión y Autenticación
  const [usuario, setUsuario] = useState<UserSession>({
    authenticated: true,
    id: 3,
    username: "cliente",
    rol: "CLIENTE",
  });
  const [modalLoginAbierto, setModalLoginAbierto] = useState<boolean>(false);
  const [loginUsername, setLoginUsername] = useState<string>("admin");
  const [loginPassword, setLoginPassword] = useState<string>("admin123");
  const [loginError, setLoginError] = useState<string>("");

  // Panel de Gestión (Admin / Vendedor)
  const [panelGestionAbierto, setPanelGestionAbierto] = useState<boolean>(false);
  const [nuevoNombreProd, setNuevoNombreProd] = useState<string>("");
  const [nuevoPrecioProd, setNuevoPrecioProd] = useState<string>("85000");
  const [nuevoStockProd, setNuevoStockProd] = useState<string>("20");
  const [nuevaCatIdProd, setNuevaCatIdProd] = useState<number>(1);
  const [nuevoMsg, setNuevoMsg] = useState<string>("");

  // Carga inicial
  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    const [prods, cats, stores] = await Promise.all([
      getProductos(),
      getCategorias(),
      getTiendasCercanas(),
    ]);

    if (prods.length > 0) setProductos(prods);
    if (cats.length > 0) setCategorias(cats);
    if (stores.length > 0) setTiendas(stores);
  };

  // Filtrado de productos
  const productosFiltrados = productos.filter((p) => {
    const coincideCat =
      categoriaActiva === "todo" ||
      (p.categoria && p.categoria.slug.toLowerCase() === categoriaActiva.toLowerCase());
    const coincideTexto =
      busquedaTexto.trim() === "" ||
      p.nombre.toLowerCase().includes(busquedaTexto.toLowerCase()) ||
      p.descripcion.toLowerCase().includes(busquedaTexto.toLowerCase());
    return coincideCat && coincideTexto;
  });

  // Manejador de Talla
  const seleccionarTalla = (productoId: number, talla: string) => {
    setTallaSeleccionada((prev) => ({ ...prev, [productoId]: talla }));
  };

  // Agregar al carrito
  const agregarAlCarrito = (producto: Producto) => {
    const talla = tallaSeleccionada[producto.id] || "M";
    setCarrito((prev) => {
      const idx = prev.findIndex(
        (item) => item.producto.id === producto.id && item.talla === talla
      );
      if (idx >= 0) {
        const clon = [...prev];
        clon[idx].cantidad += 1;
        return clon;
      }
      return [...prev, { producto, cantidad: 1, talla }];
    });
    setCarritoAbierto(true);
  };

  // Modificar cantidad en carrito
  const actualizarCantidad = (index: number, delta: number) => {
    setCarrito((prev) => {
      const clon = [...prev];
      const nueva = clon[index].cantidad + delta;
      if (nueva <= 0) {
        clon.splice(index, 1);
      } else {
        clon[index].cantidad = nueva;
      }
      return clon;
    });
  };

  const totalCarrito = carrito.reduce(
    (acc, item) => acc + Number(item.producto.precio) * item.cantidad,
    0
  );

  const totalItems = carrito.reduce((acc, item) => acc + item.cantidad, 0);

  // Ejecutar búsqueda en SerpAPI
  const buscarEnSerpApi = async (query = busquedaTienda) => {
    setCargandoTiendas(true);
    setBusquedaTienda(query);
    const resultado = await getTiendasCercanas(query, ubicacionTienda);
    setTiendas(resultado);
    setCargandoTiendas(false);
  };

  // Autenticar usuario
  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoginError("");
    try {
      const resp = await loginUser(loginUsername, loginPassword);
      setUsuario(resp);
      setModalLoginAbierto(false);
      if (resp.rol === "ADMIN" || resp.rol === "VENDEDOR") {
        cargarPedidos();
      }
    } catch (err: any) {
      setLoginError(err.message || "Error al iniciar sesión");
    }
  };

  const cambiarRolRapido = (rol: "ADMIN" | "VENDEDOR" | "CLIENTE") => {
    const userMap = {
      ADMIN: "admin",
      VENDEDOR: "vendedor",
      CLIENTE: "cliente",
    };
    setUsuario({
      authenticated: true,
      username: userMap[rol],
      rol: rol,
    });
    if (rol === "ADMIN" || rol === "VENDEDOR") {
      cargarPedidos();
    }
  };

  const cargarPedidos = async () => {
    const data = await getPedidos();
    setPedidos(data);
  };

  // Finalizar Compra
  const handleCheckout = async () => {
    if (carrito.length === 0) return;
    setProcesandoCompra(true);
    try {
      const orden = await submitOrder({
        username: usuario.username || "cliente",
        direccionEntrega: direccion,
        ciudad: ciudad,
        telefono: telefono,
        items: carrito.map((c) => ({
          productoId: c.producto.id,
          cantidad: c.cantidad,
          talla: c.talla,
        })),
      });
      setPedidoExitoso(orden);
      setCarrito([]);
      await cargarDatos();
    } catch (err: any) {
      alert("Error al tramitar la orden: " + err.message);
    } finally {
      setProcesandoCompra(false);
    }
  };

  // Crear producto desde panel de vendedor/admin
  const handleCrearProducto = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await fetch("http://localhost:8080/api/productos", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          nombre: nuevoNombreProd,
          precio: parseFloat(nuevoPrecioProd),
          stock: parseInt(nuevoStockProd),
          categoriaId: nuevaCatIdProd,
          descripcion: "Prenda de edición exclusiva NEXO Urban Collection.",
          imagenUrl: "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=600&q=80",
          tallas: "S, M, L, XL",
          destacado: true,
        }),
      });
      if (res.ok) {
        setNuevoMsg("¡Producto creado y persistido con éxito en PostgreSQL!");
        setNuevoNombreProd("");
        await cargarDatos();
        setTimeout(() => setNuevoMsg(""), 3500);
      }
    } catch {
      setNuevoMsg("Error de red al crear el producto.");
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans">
      {/* 1. BARRA SUPERIOR DE ESTADO / DIAGNÓSTICO FULL STACK */}
      <div className="bg-gradient-to-r from-blue-900 via-indigo-950 to-slate-900 border-b border-indigo-800/40 px-4 py-1.5 text-xs text-indigo-200 flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-3">
          <span className="flex items-center gap-1.5 font-semibold text-emerald-400">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
            Backend: Spring Boot 3.3.5 / Java 21 LTS (Puerto 8080)
          </span>
          <span className="hidden sm:inline text-indigo-400">|</span>
          <span className="hidden sm:flex items-center gap-1 text-slate-300">
            PostgreSQL: <strong className="text-white">tienda</strong>
          </span>
          <span className="hidden sm:inline text-indigo-400">|</span>
          <span className="hidden md:flex items-center gap-1 text-amber-300">
            SerpAPI: Google Maps Store Locator Activo
          </span>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-slate-400">Rol activo:</span>
          <span className="px-2 py-0.5 rounded text-[11px] font-bold bg-indigo-600/80 text-white border border-indigo-400/30">
            {usuario.rol} ({usuario.username})
          </span>
          <button
            onClick={() => setModalLoginAbierto(true)}
            className="text-xs text-indigo-300 hover:text-white underline ml-1 cursor-pointer"
          >
            Cambiar
          </button>
        </div>
      </div>

      {/* 2. ENCABEZADO / NAVBAR NEXO (Idéntico a la maqueta de Frameworks Backups) */}
      <header className="sticky top-0 z-40 bg-slate-950/90 backdrop-blur-md border-b border-slate-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between gap-4">
          {/* Logo / Marca */}
          <div className="flex items-center gap-3">
            <div className="bg-gradient-to-br from-amber-400 via-amber-500 to-orange-600 text-black font-black text-2xl tracking-tighter px-3.5 py-1.5 rounded-lg shadow-lg shadow-amber-500/20 flex items-center gap-1.5">
              <span>NEXO</span>
              <span className="text-xs bg-black text-amber-400 px-1 py-0.5 rounded font-mono uppercase tracking-normal">
                STORE
              </span>
            </div>
            <span className="hidden lg:inline text-xs text-slate-400 max-w-[200px] leading-tight">
              Streetwear & Geolocalización Oficial
            </span>
          </div>

          {/* Menú de Navegación */}
          <nav className="hidden md:flex items-center gap-1 bg-slate-900/80 p-1 rounded-full border border-slate-800 text-sm font-medium">
            <a
              href="#catalogo"
              className="px-4 py-2 rounded-full bg-slate-800 text-white shadow-sm hover:text-amber-400 transition"
            >
              Catálogo
            </a>
            <a
              href="#geolocalizacion"
              className="px-4 py-2 rounded-full text-slate-300 hover:text-white hover:bg-slate-800/60 transition flex items-center gap-1.5"
            >
              <MapPin className="w-4 h-4 text-amber-400" />
              Tiendas Físicas
            </a>
            <a
              href="#ofertas"
              className="px-4 py-2 rounded-full text-slate-300 hover:text-white hover:bg-slate-800/60 transition"
            >
              Ofertas
            </a>
            {(usuario.rol === "ADMIN" || usuario.rol === "VENDEDOR") && (
              <button
                onClick={() => {
                  cargarPedidos();
                  setPanelGestionAbierto(true);
                }}
                className="px-4 py-2 rounded-full bg-amber-500/20 text-amber-300 border border-amber-500/30 hover:bg-amber-500/30 transition flex items-center gap-1"
              >
                <Layers className="w-4 h-4" />
                Panel {usuario.rol}
              </button>
            )}
          </nav>

          {/* Carrito de Compras & Sesión */}
          <div className="flex items-center gap-3">
            <button
              onClick={() => setCarritoAbierto(true)}
              className="relative flex items-center gap-2.5 bg-slate-900 hover:bg-slate-800 border border-slate-700/70 px-4 py-2.5 rounded-full transition cursor-pointer shadow-sm"
              aria-label="Abrir Carrito"
            >
              <ShoppingBag className="w-5 h-5 text-amber-400" />
              <span className="hidden sm:inline text-sm font-semibold text-slate-200">
                Carrito
              </span>
              <span className="w-5 h-5 rounded-full bg-amber-500 text-slate-950 font-bold text-xs flex items-center justify-center">
                {totalItems}
              </span>
            </button>

            <button
              onClick={() => setModalLoginAbierto(true)}
              className="flex items-center gap-2 bg-slate-900 hover:bg-slate-800 border border-slate-700 px-3.5 py-2.5 rounded-full text-xs font-medium text-slate-300 hover:text-white transition cursor-pointer"
            >
              <User className="w-4 h-4 text-indigo-400" />
              <span className="hidden sm:inline font-mono">{usuario.username}</span>
            </button>
          </div>
        </div>
      </header>

      {/* 3. HERO BANNER PRINCIPAL NEXO */}
      <section className="relative overflow-hidden bg-gradient-to-b from-slate-900 via-slate-950 to-slate-950 border-b border-slate-800/80 py-16 px-4 sm:px-6 lg:px-8">
        <div className="absolute inset-0 opacity-20 bg-[radial-gradient(#f59e0b_1px,transparent_1px)] [background-size:24px_24px] pointer-events-none"></div>
        <div className="max-w-7xl mx-auto relative z-10 flex flex-col md:flex-row items-center justify-between gap-10">
          <div className="max-w-2xl text-center md:text-left">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-amber-500/10 border border-amber-500/30 text-amber-400 text-xs font-semibold uppercase tracking-wider mb-4">
              <Sparkles className="w-3.5 h-3.5" />
              Colección Urbana 2026 • Full Stack Spring Boot & Next.js
            </div>
            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-black tracking-tight text-white leading-none">
              ESTILO URBANO. <br />
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-amber-400 via-orange-500 to-amber-200">
                IDENTIDAD NEXO.
              </span>
            </h1>
            <p className="mt-4 text-base sm:text-lg text-slate-300 leading-relaxed">
              Catálogo premium de indumentaria contemporánea con persistencia en <strong>PostgreSQL</strong>,
              seguridad <strong>Spring Security 6</strong> y geolocalización de tiendas físicas impulsada por la API de <strong>SerpAPI Google Maps</strong>.
            </p>
            <div className="mt-8 flex flex-wrap items-center justify-center md:justify-start gap-4">
              <a
                href="#catalogo"
                className="px-6 py-3.5 rounded-xl bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold text-sm shadow-lg shadow-amber-500/25 transition flex items-center gap-2 cursor-pointer"
              >
                Ver Prendas Disponibles
                <ArrowRight className="w-4 h-4" />
              </a>
              <a
                href="#geolocalizacion"
                className="px-6 py-3.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-200 border border-slate-700 font-semibold text-sm transition flex items-center gap-2 cursor-pointer"
              >
                <MapPin className="w-4 h-4 text-amber-400" />
                Buscar Tiendas Físicas
              </a>
            </div>
          </div>

          <div className="relative w-full max-w-sm aspect-square rounded-2xl overflow-hidden border border-slate-700/60 shadow-2xl bg-slate-900">
            <Image
              src="https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=800&q=80"
              alt="Camiseta NEXO Streetwear"
              fill
              className="object-cover hover:scale-105 transition-transform duration-500"
              priority
            />
            <div className="absolute bottom-0 inset-x-0 p-4 bg-gradient-to-t from-slate-950 via-slate-950/80 to-transparent">
              <span className="text-xs text-amber-400 font-mono font-semibold">DESTACADO 2026</span>
              <p className="text-sm font-bold text-white">Camiseta NEXO 01 Oversize Black</p>
              <p className="text-xs text-slate-300">$ 79.000 COP • Despacho inmediato</p>
            </div>
          </div>
        </div>
      </section>

      {/* 4. SECCIÓN CATÁLOGO - PRENDAS DISPONIBLES (Maqueta NEXO) */}
      <section id="catalogo" className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 w-full">
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 pb-6 border-b border-slate-800">
          <div>
            <span className="text-xs font-mono font-semibold uppercase tracking-wider text-amber-400">
              CATÁLOGO OFICIAL
            </span>
            <h2 className="text-3xl font-black text-white tracking-tight mt-1">
              Prendas disponibles
            </h2>
            <p className="text-sm text-slate-400 mt-1">
              Explora nuestra selección de indumentaria urbana con corte y diseño exclusivo.
            </p>
          </div>

          {/* Barra de Búsqueda rápida */}
          <div className="relative w-full md:w-72">
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Buscar prenda o estilo..."
              value={busquedaTexto}
              onChange={(e) => setBusquedaTexto(e.target.value)}
              className="w-full bg-slate-900 border border-slate-800 rounded-xl pl-10 pr-4 py-2.5 text-xs text-slate-200 placeholder:text-slate-500 focus:outline-none focus:border-amber-500 transition"
            />
            {busquedaTexto && (
              <button
                onClick={() => setBusquedaTexto("")}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-slate-400 hover:text-white"
              >
                ✕
              </button>
            )}
          </div>
        </div>

        {/* Filtros de Categoría (Idéntico a Mockup: Todo, Camisetas, Hoodies, Polos, Billeteras, Gorras) */}
        <div className="flex items-center gap-2 overflow-x-auto py-6 no-scrollbar">
          <button
            onClick={() => setCategoriaActiva("todo")}
            className={`px-5 py-2.5 rounded-full text-xs font-bold transition whitespace-nowrap cursor-pointer ${
              categoriaActiva === "todo"
                ? "bg-amber-500 text-slate-950 shadow-md shadow-amber-500/20"
                : "bg-slate-900 text-slate-300 hover:bg-slate-800 border border-slate-800"
            }`}
          >
            Todo
          </button>
          {categorias.map((cat) => (
            <button
              key={cat.id}
              onClick={() => setCategoriaActiva(cat.slug)}
              className={`px-5 py-2.5 rounded-full text-xs font-bold transition whitespace-nowrap cursor-pointer ${
                categoriaActiva === cat.slug
                  ? "bg-amber-500 text-slate-950 shadow-md shadow-amber-500/20"
                  : "bg-slate-900 text-slate-300 hover:bg-slate-800 border border-slate-800"
              }`}
            >
              {cat.nombre}
            </button>
          ))}
        </div>

        {/* Resumen de Productos Encontrados */}
        <div className="flex items-center justify-between text-xs text-slate-400 mb-6">
          <span>
            Mostrando <strong>{productosFiltrados.length}</strong> prendas en el catálogo
          </span>
          {categoriaActiva !== "todo" && (
            <span className="text-amber-400 font-mono">
              Filtro activo: <strong>{categoriaActiva.toUpperCase()}</strong>
            </span>
          )}
        </div>

        {/* Grid de Productos */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {productosFiltrados.map((producto) => {
            const tallaActual = tallaSeleccionada[producto.id] || "M";
            return (
              <div
                key={producto.id}
                className="group bg-slate-900/90 border border-slate-800 rounded-2xl overflow-hidden hover:border-amber-500/50 transition-all duration-300 flex flex-col justify-between shadow-lg"
              >
                <div>
                  {/* Imagen del Producto */}
                  <div className="relative aspect-[4/5] w-full bg-slate-950 overflow-hidden">
                    <Image
                      src={producto.imagenUrl || "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=600&q=80"}
                      alt={producto.nombre}
                      fill
                      className="object-cover group-hover:scale-105 transition-transform duration-500"
                    />
                    {producto.destacado && (
                      <span className="absolute top-3 left-3 bg-amber-500 text-slate-950 text-[10px] font-black px-2 py-0.5 rounded tracking-wider uppercase">
                        POPULAR
                      </span>
                    )}
                    <span className="absolute top-3 right-3 bg-slate-950/80 backdrop-blur-md text-slate-200 text-[11px] font-mono px-2 py-0.5 rounded border border-slate-700">
                      Stock: {producto.stock}
                    </span>
                  </div>

                  {/* Información del Producto */}
                  <div className="p-4">
                    <span className="text-[11px] font-mono text-amber-400 uppercase">
                      {producto.categoria?.nombre || "Colección NEXO"}
                    </span>
                    <h3 className="font-bold text-white text-base mt-1 line-clamp-1">
                      {producto.nombre}
                    </h3>
                    <p className="text-xs text-slate-400 mt-1 line-clamp-2 leading-relaxed">
                      {producto.descripcion}
                    </p>

                    <div className="mt-3 flex items-baseline justify-between">
                      <span className="text-lg font-black text-white">
                        {formatPrice(producto.precio)}
                      </span>
                      <span className="text-[11px] text-slate-500">COP</span>
                    </div>

                    {/* Selector de Tallas */}
                    <div className="mt-3">
                      <span className="text-[10px] font-mono text-slate-400 uppercase">
                        Tallas disponibles:
                      </span>
                      <div className="flex items-center gap-1.5 mt-1.5">
                        {["S", "M", "L", "XL"].map((talla) => (
                          <button
                            key={talla}
                            onClick={() => seleccionarTalla(producto.id, talla)}
                            className={`w-7 h-7 rounded text-[11px] font-bold transition cursor-pointer flex items-center justify-center ${
                              tallaActual === talla
                                ? "bg-amber-500 text-slate-950"
                                : "bg-slate-800 text-slate-300 hover:bg-slate-700 border border-slate-700"
                            }`}
                          >
                            {talla}
                          </button>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Botón de Compra */}
                <div className="p-4 pt-0">
                  <button
                    onClick={() => agregarAlCarrito(producto)}
                    disabled={producto.stock <= 0}
                    className={`w-full py-2.5 rounded-xl font-bold text-xs flex items-center justify-center gap-2 transition cursor-pointer ${
                      producto.stock > 0
                        ? "bg-amber-500 hover:bg-amber-400 text-slate-950 shadow-md shadow-amber-500/20"
                        : "bg-slate-800 text-slate-500 cursor-not-allowed"
                    }`}
                  >
                    <ShoppingBag className="w-4 h-4" />
                    {producto.stock > 0 ? "Agregar al carrito" : "Agotado temporal"}
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      </section>

      {/* 5. SECCIÓN GEOLOCALIZACIÓN NEXO (SerpAPI Google Maps - Mockup oficial) */}
      <section id="geolocalizacion" className="bg-slate-900/60 border-t border-b border-slate-800 py-16 px-4 sm:px-6 lg:px-8 w-full">
        <div className="max-w-7xl mx-auto">
          {/* Encabezado de Sección */}
          <div className="text-center max-w-3xl mx-auto">
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-500/10 border border-blue-500/30 text-blue-400 text-xs font-semibold uppercase tracking-wider mb-2">
              <MapPin className="w-3.5 h-3.5" />
              GEOLOCALIZACIÓN NEXO • SerpAPI Google Maps
            </span>
            <h2 className="text-3xl sm:text-4xl font-black text-white tracking-tight">
              Encuentra tiendas de ropa
            </h2>
            <p className="text-sm text-slate-400 mt-2">
              Descubre establecimientos cercanos y consulta su información de ubicación, horarios y calificaciones obtenidas mediante la API de SerpAPI.
            </p>
          </div>

          {/* Barra de Búsqueda API SerpApi (Google Maps) */}
          <div className="mt-8 max-w-3xl mx-auto bg-slate-900 border border-slate-700/80 rounded-2xl p-4 shadow-xl">
            <div className="flex flex-col sm:flex-row gap-3">
              <div className="relative flex-1">
                <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  placeholder="¿Qué estás buscando? (ej. tiendas de ropa, streetwear, boutique)..."
                  value={busquedaTienda}
                  onChange={(e) => setBusquedaTienda(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-10 pr-4 py-3 text-xs text-slate-100 placeholder:text-slate-500 focus:outline-none focus:border-blue-500"
                />
              </div>

              <div className="relative w-full sm:w-56">
                <MapPin className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  placeholder="Ubicación (Villavicencio)..."
                  value={ubicacionTienda}
                  onChange={(e) => setUbicacionTienda(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-9 pr-3 py-3 text-xs text-slate-100 focus:outline-none focus:border-blue-500"
                />
              </div>

              <button
                onClick={() => buscarEnSerpApi(busquedaTienda)}
                disabled={cargandoTiendas}
                className="px-6 py-3 bg-blue-600 hover:bg-blue-500 text-white rounded-xl text-xs font-bold transition flex items-center justify-center gap-2 cursor-pointer shadow-lg shadow-blue-600/25 disabled:opacity-50"
              >
                {cargandoTiendas ? "Consultando..." : "Buscar"}
              </button>
            </div>

            {/* Filtros de Sugerencia (Chips) */}
            <div className="mt-3 pt-3 border-t border-slate-800/80 flex flex-wrap items-center gap-2">
              <span className="text-[10px] font-mono text-slate-500 uppercase tracking-wider">
                SUGERENCIAS:
              </span>
              {["tiendas de ropa", "streetwear", "boutique", "zapaterías", "cafeterías"].map((sug) => (
                <button
                  key={sug}
                  onClick={() => buscarEnSerpApi(sug)}
                  className="px-2.5 py-1 rounded-md text-[11px] font-medium bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white transition cursor-pointer"
                >
                  {sug}
                </button>
              ))}
            </div>
          </div>

          {/* Resumen de Resultados */}
          <div className="mt-8 flex items-center justify-between text-xs text-slate-400 max-w-7xl mx-auto">
            <span>
              Mostrando <strong>{tiendas.length}</strong> tiendas encontradas en la zona
            </span>
            <span className="bg-slate-800/80 px-2.5 py-1 rounded text-[11px] font-mono text-blue-300 border border-slate-700">
              Búsqueda activa: "{busquedaTienda}"
            </span>
          </div>

          {/* Grid de Tiendas */}
          <div className="mt-6 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {tiendas.map((tienda) => (
              <div
                key={tienda.id}
                className="bg-slate-950 border border-slate-800 rounded-2xl overflow-hidden hover:border-blue-500/50 transition flex flex-col justify-between shadow-lg"
              >
                <div>
                  <div className="relative aspect-video w-full bg-slate-900">
                    <Image
                      src={tienda.thumbnail || "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=500&q=80"}
                      alt={tienda.title}
                      fill
                      className="object-cover"
                    />
                    {tienda.nexoRegistrado && (
                      <span className="absolute top-2.5 right-2.5 bg-amber-500 text-slate-950 text-[10px] font-black px-2 py-0.5 rounded shadow">
                        NEXO REGISTRADO
                      </span>
                    )}
                  </div>

                  <div className="p-4">
                    <div className="flex items-center gap-1.5 text-amber-400 text-xs font-bold">
                      <Star className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
                      <span>{tienda.rating || 4.5}</span>
                      <span className="text-slate-500 font-normal">
                        ({tienda.reviews || 120} reseñas)
                      </span>
                    </div>

                    <h3 className="font-bold text-white text-sm mt-1.5 line-clamp-1">
                      {tienda.title}
                    </h3>

                    <div className="mt-2.5 space-y-1.5 text-xs text-slate-400">
                      <div className="flex items-start gap-1.5">
                        <MapPin className="w-3.5 h-3.5 text-slate-500 shrink-0 mt-0.5" />
                        <span className="line-clamp-2">{tienda.address}</span>
                      </div>
                      <div className="flex items-center gap-1.5">
                        <Phone className="w-3.5 h-3.5 text-slate-500 shrink-0" />
                        <span>{tienda.phone || "+57 (8) 660-0000"}</span>
                      </div>
                      <div className="flex items-center gap-1.5">
                        <Clock className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
                        <span className="text-emerald-400 font-medium">
                          {tienda.openState || "Abierto"}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="p-4 pt-0">
                  <a
                    href={`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(
                      tienda.title + " " + tienda.address
                    )}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="w-full py-2 bg-slate-900 hover:bg-slate-800 text-slate-300 hover:text-white rounded-xl text-xs font-semibold transition flex items-center justify-center gap-1.5 border border-slate-800"
                  >
                    <span>Ver en Google Maps</span>
                    <ExternalLink className="w-3.5 h-3.5" />
                  </a>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* 6. MODAL / DRAWER DEL CARRITO DE COMPRAS */}
      {carritoAbierto && (
        <div className="fixed inset-0 z-50 flex justify-end bg-black/70 backdrop-blur-sm animate-fade-in">
          <div className="w-full max-w-md bg-slate-950 border-l border-slate-800 h-full flex flex-col justify-between p-6 shadow-2xl">
            <div>
              <div className="flex items-center justify-between pb-4 border-b border-slate-800">
                <div className="flex items-center gap-2">
                  <ShoppingBag className="w-5 h-5 text-amber-400" />
                  <h3 className="text-lg font-bold text-white">Carrito de Compras</h3>
                  <span className="text-xs text-slate-400">({totalItems} ítems)</span>
                </div>
                <button
                  onClick={() => setCarritoAbierto(false)}
                  className="text-slate-400 hover:text-white text-lg font-bold cursor-pointer"
                >
                  ✕
                </button>
              </div>

              {/* Lista de Ítems */}
              <div className="mt-4 divide-y divide-slate-800 max-h-[45vh] overflow-y-auto pr-1">
                {carrito.length === 0 ? (
                  <div className="py-12 text-center text-slate-500">
                    <ShoppingBag className="w-12 h-12 mx-auto mb-2 opacity-30" />
                    <p className="text-sm">Tu carrito de compras está vacío.</p>
                  </div>
                ) : (
                  carrito.map((item, idx) => (
                    <div key={idx} className="py-3 flex items-center gap-3">
                      <div className="relative w-14 h-16 bg-slate-900 rounded-lg overflow-hidden shrink-0 border border-slate-800">
                        <Image
                          src={item.producto.imagenUrl || ""}
                          alt={item.producto.nombre}
                          fill
                          className="object-cover"
                        />
                      </div>
                      <div className="flex-1 min-w-0">
                        <h4 className="text-xs font-bold text-white truncate">
                          {item.producto.nombre}
                        </h4>
                        <span className="text-[11px] text-amber-400 font-mono">
                          Talla: {item.talla} • {formatPrice(item.producto.precio)}
                        </span>
                        <div className="flex items-center gap-2 mt-1">
                          <button
                            onClick={() => actualizarCantidad(idx, -1)}
                            className="w-5 h-5 rounded bg-slate-800 hover:bg-slate-700 flex items-center justify-center text-xs text-white"
                          >
                            <Minus className="w-3 h-3" />
                          </button>
                          <span className="text-xs font-mono font-bold">{item.cantidad}</span>
                          <button
                            onClick={() => actualizarCantidad(idx, 1)}
                            className="w-5 h-5 rounded bg-slate-800 hover:bg-slate-700 flex items-center justify-center text-xs text-white"
                          >
                            <Plus className="w-3 h-3" />
                          </button>
                        </div>
                      </div>
                      <div className="text-right">
                        <span className="text-xs font-bold text-white block font-mono">
                          {formatPrice(Number(item.producto.precio) * item.cantidad)}
                        </span>
                        <button
                          onClick={() => actualizarCantidad(idx, -item.cantidad)}
                          className="text-[11px] text-red-400 hover:text-red-300 mt-1 cursor-pointer"
                        >
                          <Trash2 className="w-3.5 h-3.5 inline" />
                        </button>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>

            {/* Checkout & Formulario de Envío */}
            {carrito.length > 0 && (
              <div className="pt-4 border-t border-slate-800 space-y-3">
                <div className="bg-slate-900/80 p-3 rounded-xl border border-slate-800 space-y-2">
                  <span className="text-[10px] font-mono text-slate-400 uppercase tracking-wider block">
                    DATOS DE ENTREGA (POSTGRESQL ORDER):
                  </span>
                  <input
                    type="text"
                    placeholder="Dirección de entrega"
                    value={direccion}
                    onChange={(e) => setDireccion(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-700 rounded-lg px-2.5 py-1.5 text-xs text-white"
                  />
                  <div className="grid grid-cols-2 gap-2">
                    <input
                      type="text"
                      placeholder="Ciudad"
                      value={ciudad}
                      onChange={(e) => setCiudad(e.target.value)}
                      className="bg-slate-950 border border-slate-700 rounded-lg px-2.5 py-1.5 text-xs text-white"
                    />
                    <input
                      type="text"
                      placeholder="Teléfono"
                      value={telefono}
                      onChange={(e) => setTelefono(e.target.value)}
                      className="bg-slate-950 border border-slate-700 rounded-lg px-2.5 py-1.5 text-xs text-white"
                    />
                  </div>
                </div>

                <div className="flex items-center justify-between text-sm font-bold">
                  <span>Gran Total:</span>
                  <span className="text-amber-400 font-mono text-base">
                    {formatPrice(totalCarrito)}
                  </span>
                </div>

                <button
                  onClick={handleCheckout}
                  disabled={procesandoCompra}
                  className="w-full py-3 bg-amber-500 hover:bg-amber-400 text-slate-950 rounded-xl font-bold text-xs uppercase tracking-wider transition cursor-pointer shadow-lg shadow-amber-500/20 disabled:opacity-50"
                >
                  {procesandoCompra ? "Procesando en Spring Boot..." : "Confirmar Pedido (Checkout)"}
                </button>
              </div>
            )}
          </div>
        </div>
      )}

      {/* 7. MODAL DE PEDIDO CONFIRMADO EXITOSAMENTE */}
      {pedidoExitoso && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4">
          <div className="max-w-md w-full bg-slate-900 border border-emerald-500/40 rounded-2xl p-6 text-center shadow-2xl">
            <CheckCircle className="w-14 h-14 text-emerald-400 mx-auto mb-3" />
            <h3 className="text-xl font-bold text-white">¡Pedido Registrado con Éxito!</h3>
            <p className="text-xs text-slate-300 mt-2">
              Tu orden ha sido persistida en PostgreSQL y vinculada a tu cuenta de usuario.
            </p>
            <div className="mt-4 bg-slate-950 p-3.5 rounded-xl border border-slate-800 text-left text-xs font-mono space-y-1.5">
              <p><span className="text-slate-500">No. Orden:</span> <strong className="text-amber-400">{pedidoExitoso.numeroPedido}</strong></p>
              <p><span className="text-slate-500">Cliente:</span> {pedidoExitoso.usuario?.username || usuario.username}</p>
              <p><span className="text-slate-500">Total:</span> {formatPrice(pedidoExitoso.total)}</p>
              <p><span className="text-slate-500">Estado:</span> <strong className="text-emerald-400">{pedidoExitoso.estado}</strong></p>
              <p><span className="text-slate-500">Destino:</span> {pedidoExitoso.direccionEntrega}, {pedidoExitoso.ciudad}</p>
            </div>
            <button
              onClick={() => {
                setPedidoExitoso(null);
                setCarritoAbierto(false);
              }}
              className="mt-5 w-full py-2.5 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold text-xs rounded-xl transition"
            >
              Continuar Comprando
            </button>
          </div>
        </div>
      )}

      {/* 8. MODAL DE GESTIÓN Y ROLES (ADMIN / VENDEDOR / CLIENTE) */}
      {modalLoginAbierto && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4">
          <div className="max-w-md w-full bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-2xl">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <Shield className="w-5 h-5 text-indigo-400" />
                <h3 className="text-base font-bold text-white">Control de Acceso y Roles</h3>
              </div>
              <button onClick={() => setModalLoginAbierto(false)} className="text-slate-400 hover:text-white">✕</button>
            </div>

            {/* Selector Rápido para Demostración Académica */}
            <div className="mt-4">
              <span className="text-[11px] font-mono text-slate-400 uppercase block mb-2">
                CAMBIO RÁPIDO DE ROL (ENTORNO DE EVALUACIÓN):
              </span>
              <div className="grid grid-cols-3 gap-2">
                {(["CLIENTE", "VENDEDOR", "ADMIN"] as const).map((r) => (
                  <button
                    key={r}
                    onClick={() => {
                      cambiarRolRapido(r);
                      setModalLoginAbierto(false);
                    }}
                    className={`py-2 px-2 rounded-xl text-xs font-bold transition ${
                      usuario.rol === r
                        ? "bg-indigo-600 text-white"
                        : "bg-slate-800 text-slate-300 hover:bg-slate-700 border border-slate-700"
                    }`}
                  >
                    {r}
                  </button>
                ))}
              </div>
            </div>

            {/* Formulario Estricto contra Backend */}
            <form onSubmit={handleLogin} className="mt-6 pt-4 border-t border-slate-800 space-y-3">
              <span className="text-[11px] font-mono text-slate-400 uppercase block">
                O AUTENTICARSE CON CREDENCIALES EN POSTGRESQL:
              </span>
              <div>
                <label className="text-xs text-slate-300 block mb-1">Usuario:</label>
                <input
                  type="text"
                  value={loginUsername}
                  onChange={(e) => setLoginUsername(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-xs text-white"
                />
              </div>
              <div>
                <label className="text-xs text-slate-300 block mb-1">Contraseña:</label>
                <input
                  type="password"
                  value={loginPassword}
                  onChange={(e) => setLoginPassword(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-xs text-white"
                />
              </div>

              {loginError && (
                <p className="text-xs text-red-400 bg-red-950/40 border border-red-800/40 p-2 rounded">
                  {loginError}
                </p>
              )}

              <button
                type="submit"
                className="w-full py-2.5 bg-indigo-600 hover:bg-indigo-500 text-white font-bold text-xs rounded-xl transition"
              >
                Iniciar Sesión en Spring Security
              </button>
            </form>
          </div>
        </div>
      )}

      {/* 9. MODAL DE PANEL DE GESTIÓN (ADMIN Y VENDEDOR) */}
      {panelGestionAbierto && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4">
          <div className="max-w-2xl w-full bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-2xl max-h-[85vh] overflow-y-auto">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <Layers className="w-5 h-5 text-amber-400" />
                <h3 className="text-base font-bold text-white">
                  Panel de Control — Rol {usuario.rol}
                </h3>
              </div>
              <button onClick={() => setPanelGestionAbierto(false)} className="text-slate-400 hover:text-white">✕</button>
            </div>

            {/* Crear nuevo producto (VENDEDOR o ADMIN) */}
            <div className="mt-4 bg-slate-950 p-4 rounded-xl border border-slate-800">
              <h4 className="text-xs font-mono font-bold text-amber-400 uppercase">
                + Crear Nueva Prenda en el Catálogo (POST /api/productos)
              </h4>
              <form onSubmit={handleCrearProducto} className="mt-3 grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
                <div>
                  <label className="text-slate-400 block mb-1">Nombre de la Prenda:</label>
                  <input
                    type="text"
                    required
                    placeholder="Ej. Hoodie NEXO Limited Acid"
                    value={nuevoNombreProd}
                    onChange={(e) => setNuevoNombreProd(e.target.value)}
                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-2.5 py-1.5 text-white"
                  />
                </div>
                <div>
                  <label className="text-slate-400 block mb-1">Categoría:</label>
                  <select
                    value={nuevaCatIdProd}
                    onChange={(e) => setNuevaCatIdProd(parseInt(e.target.value))}
                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-2.5 py-1.5 text-white"
                  >
                    {categorias.map((c) => (
                      <option key={c.id} value={c.id}>{c.nombre}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="text-slate-400 block mb-1">Precio (COP):</label>
                  <input
                    type="number"
                    value={nuevoPrecioProd}
                    onChange={(e) => setNuevoPrecioProd(e.target.value)}
                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-2.5 py-1.5 text-white"
                  />
                </div>
                <div>
                  <label className="text-slate-400 block mb-1">Stock Inicial:</label>
                  <input
                    type="number"
                    value={nuevoStockProd}
                    onChange={(e) => setNuevoStockProd(e.target.value)}
                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-2.5 py-1.5 text-white"
                  />
                </div>
                <div className="sm:col-span-2">
                  <button
                    type="submit"
                    className="w-full py-2 bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold rounded-lg transition"
                  >
                    Guardar Prenda en Base de Datos
                  </button>
                </div>
              </form>
              {nuevoMsg && <p className="text-xs text-emerald-400 mt-2 font-mono">{nuevoMsg}</p>}
            </div>

            {/* Listado de Pedidos Recientes */}
            <div className="mt-6">
              <h4 className="text-xs font-mono font-bold text-slate-300 uppercase mb-2">
                Historial de Órdenes Recientes en PostgreSQL (GET /api/pedidos)
              </h4>
              <div className="space-y-2 max-h-48 overflow-y-auto">
                {pedidos.length === 0 ? (
                  <p className="text-xs text-slate-500">No hay pedidos registrados aún.</p>
                ) : (
                  pedidos.map((ped) => (
                    <div key={ped.id} className="bg-slate-950 p-3 rounded-lg border border-slate-800 text-xs flex justify-between items-center">
                      <div>
                        <strong className="text-amber-400 font-mono">{ped.numeroPedido}</strong>
                        <span className="text-slate-400 ml-2">Cliente: {ped.usuario?.username}</span>
                        <p className="text-[11px] text-slate-500">{ped.direccionEntrega}, {ped.ciudad}</p>
                      </div>
                      <div className="text-right">
                        <span className="text-white font-mono font-bold block">{formatPrice(ped.total)}</span>
                        <span className="text-[10px] px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                          {ped.estado}
                        </span>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 10. PIE DE PÁGINA INSTITUCIONAL & ACADÉMICO */}
      <footer className="mt-auto bg-slate-950 border-t border-slate-800/80 py-10 px-4 sm:px-6 lg:px-8 text-xs text-slate-400">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-6">
          <div>
            <div className="flex items-center gap-2">
              <span className="font-black text-lg text-white">NEXO STORE</span>
              <span className="text-slate-600">|</span>
              <span className="text-amber-400 font-medium">Proyecto Final de Frameworks Web</span>
            </div>
            <p className="mt-1 text-slate-500 max-w-md">
              Desarrollado por <strong>Juan Pablo Bustos Jara</strong> — UNIMINUTO Villavicencio, Meta.
              Tecnologías integradas: Spring Boot 3.3.5, Spring Security 6, PostgreSQL, Next.js, React 19, Tailwind CSS y SerpAPI.
            </p>
          </div>

          <div className="flex items-center gap-4 text-xs font-mono text-slate-400">
            <a href="http://localhost:8080/swagger-ui.html" target="_blank" className="hover:text-amber-400 transition">
              Swagger API Docs
            </a>
            <span>•</span>
            <a href="http://localhost:8080/api/productos" target="_blank" className="hover:text-amber-400 transition">
              REST /productos
            </a>
            <span>•</span>
            <a href="http://localhost:8080/api/serpapi/tiendas" target="_blank" className="hover:text-amber-400 transition">
              SerpAPI /tiendas
            </a>
          </div>
        </div>
      </footer>
    </div>
  );
}
