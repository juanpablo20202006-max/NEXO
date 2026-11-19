import { getImageUrl, getProducts, getProductBySlug } from '../lib/strapi';
import { formatPrice } from '../lib/utils';

async function runTests() {
  console.log('🧪 Iniciando pruebas de integración automatizadas...\n');
  let passed = 0;
  let failed = 0;

  function assert(condition: boolean, testName: string) {
    if (condition) {
      console.log(`✅ [PASS] ${testName}`);
      passed++;
    } else {
      console.error(`❌ [FAIL] ${testName}`);
      failed++;
    }
  }

  // Test 1: getImageUrl con ruta relativa
  try {
    const url = getImageUrl('/uploads/test.jpg');
    assert(url === 'http://localhost:1337/uploads/test.jpg', 'getImageUrl maneja rutas relativas (/uploads/...)');
  } catch (e: any) {
    assert(false, `getImageUrl relativa falló: ${e.message}`);
  }

  // Test 2: getImageUrl con URL absoluta
  try {
    const absUrl = getImageUrl('https://cdn.example.com/foto.jpg');
    assert(absUrl === 'https://cdn.example.com/foto.jpg', 'getImageUrl conserva URLs absolutas');
  } catch (e: any) {
    assert(false, `getImageUrl absoluta falló: ${e.message}`);
  }

  // Test 3: formatPrice en COP
  try {
    const formatted = formatPrice(320000);
    assert(formatted.includes('320.000') || formatted.includes('320,000'), 'formatPrice formatea números a moneda');
  } catch (e: any) {
    assert(false, `formatPrice falló: ${e.message}`);
  }

  // Test 4: getProducts devuelve al menos 6 productos reales de PostgreSQL vía Strapi
  let sampleSlug = '';
  try {
    const products = await getProducts();
    assert(Array.isArray(products), 'getProducts retorna un arreglo');
    assert(products.length >= 6, `getProducts retorna al menos 6 productos (actual: ${products.length})`);
    
    const first = products[0];
    sampleSlug = first.slug;
    assert(typeof first.documentId === 'string' && first.documentId.length > 0, 'Cada producto incluye documentId de Strapi 5');
    assert(typeof first.price === 'number', 'El precio se convierte y tipifica como number');
    assert(first.image !== null && typeof first.image.url === 'string', 'Cada producto tiene una imagen válida asociada');
  } catch (e: any) {
    assert(false, `getProducts falló: ${e.message}`);
  }

  // Test 5: getProductBySlug con slug existente
  try {
    if (sampleSlug) {
      const product = await getProductBySlug(sampleSlug);
      assert(product !== null, `getProductBySlug encuentra el producto '${sampleSlug}'`);
      assert(product?.slug === sampleSlug, 'El slug del producto retornado coincide');
    }
  } catch (e: any) {
    assert(false, `getProductBySlug existente falló: ${e.message}`);
  }

  // Test 6: getProductBySlug con slug inexistente retorna null
  try {
    const nonExistent = await getProductBySlug('producto-inexistente-xyz-404');
    assert(nonExistent === null, 'getProductBySlug retorna null para productos inexistentes');
  } catch (e: any) {
    assert(false, `getProductBySlug inexistente falló: ${e.message}`);
  }

  console.log(`\n📊 Resumen de pruebas: ${passed} pasadas, ${failed} fallidas`);
  if (failed > 0) {
    process.exit(1);
  }
}

runTests().catch(err => {
  console.error('Error no controlado en suite de pruebas:', err);
  process.exit(1);
});
