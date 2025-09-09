package kr.cjs.catty.view

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.ViewInfo
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass
import androidx.room.util.TableInfo.Companion.read as tableInfoRead
import androidx.room.util.ViewInfo.Companion.read as viewInfoRead

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class CattyDatabase_Impl : CattyDatabase() {
  private val _productDao: Lazy<ProductDao> = lazy {
    ProductDao_Impl(this)
  }

  private val _departmentDao: Lazy<DepartmentDao> = lazy {
    DepartmentDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1,
        "79fc329684badd48bf98be286de5a879", "ddeccd0c41c059436d3c07eefb266eb8") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `product` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `productName` TEXT NOT NULL, `quantity` INTEGER NOT NULL, `departmentId` INTEGER, FOREIGN KEY(`departmentId`) REFERENCES `department`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_product_departmentId` ON `product` (`departmentId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `department` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `departmentName` TEXT NOT NULL)")
        connection.execSQL("CREATE VIEW `ProductDetail` AS SELECT p.id, d.departmentName, p.productName, p.quantity FROM department as d INNER JOIN product as p ON d.id = p.departmentId ORDER BY p.id DESC")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '79fc329684badd48bf98be286de5a879')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `product`")
        connection.execSQL("DROP TABLE IF EXISTS `department`")
        connection.execSQL("DROP VIEW IF EXISTS `ProductDetail`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsProduct: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsProduct.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProduct.put("productName", TableInfo.Column("productName", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProduct.put("quantity", TableInfo.Column("quantity", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProduct.put("departmentId", TableInfo.Column("departmentId", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysProduct: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysProduct.add(TableInfo.ForeignKey("department", "SET NULL", "NO ACTION",
            listOf("departmentId"), listOf("id")))
        val _indicesProduct: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesProduct.add(TableInfo.Index("index_product_departmentId", false,
            listOf("departmentId"), listOf("ASC")))
        val _infoProduct: TableInfo = TableInfo("product", _columnsProduct, _foreignKeysProduct,
            _indicesProduct)
        val _existingProduct: TableInfo = tableInfoRead(connection, "product")
        if (!_infoProduct.equals(_existingProduct)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |product(kr.cjs.catty.view.Product).
              | Expected:
              |""".trimMargin() + _infoProduct + """
              |
              | Found:
              |""".trimMargin() + _existingProduct)
        }
        val _columnsDepartment: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDepartment.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDepartment.put("departmentName", TableInfo.Column("departmentName", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDepartment: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDepartment: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoDepartment: TableInfo = TableInfo("department", _columnsDepartment,
            _foreignKeysDepartment, _indicesDepartment)
        val _existingDepartment: TableInfo = tableInfoRead(connection, "department")
        if (!_infoDepartment.equals(_existingDepartment)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |department(kr.cjs.catty.view.Department).
              | Expected:
              |""".trimMargin() + _infoDepartment + """
              |
              | Found:
              |""".trimMargin() + _existingDepartment)
        }
        val _infoProductDetail: ViewInfo = ViewInfo("ProductDetail",
            "CREATE VIEW `ProductDetail` AS SELECT p.id, d.departmentName, p.productName, p.quantity FROM department as d INNER JOIN product as p ON d.id = p.departmentId ORDER BY p.id DESC")
        val _existingProductDetail: ViewInfo = viewInfoRead(connection, "ProductDetail")
        if (!_infoProductDetail.equals(_existingProductDetail)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |ProductDetail(kr.cjs.catty.view.ProductDetail).
              | Expected:
              |""".trimMargin() + _infoProductDetail + """
              |
              | Found:
              |""".trimMargin() + _existingProductDetail)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    val _tables: MutableSet<String> = mutableSetOf()
    _tables.add("department")
    _tables.add("product")
    _viewTables.put("productdetail", _tables)
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "product", "department")
  }

  public override fun clearAllTables() {
    super.performClear(true, "product", "department")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(ProductDao::class, ProductDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(DepartmentDao::class, DepartmentDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun ProductDao(): ProductDao = _productDao.value

  public override fun DepartmentDao(): DepartmentDao = _departmentDao.value
}
