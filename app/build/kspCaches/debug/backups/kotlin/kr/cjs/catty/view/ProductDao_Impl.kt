package kr.cjs.catty.view

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ProductDao_Impl(
  __db: RoomDatabase,
) : ProductDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfProduct: EntityInsertAdapter<Product>

  private val __deleteAdapterOfProduct: EntityDeleteOrUpdateAdapter<Product>

  private val __updateAdapterOfProduct: EntityDeleteOrUpdateAdapter<Product>
  init {
    this.__db = __db
    this.__insertAdapterOfProduct = object : EntityInsertAdapter<Product>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `product` (`id`,`productName`,`quantity`,`departmentId`) VALUES (nullif(?, 0),?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Product) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.productName)
        statement.bindLong(3, entity.quantity.toLong())
        val _tmpDepartmentId: Int? = entity.departmentId
        if (_tmpDepartmentId == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmpDepartmentId.toLong())
        }
      }
    }
    this.__deleteAdapterOfProduct = object : EntityDeleteOrUpdateAdapter<Product>() {
      protected override fun createQuery(): String = "DELETE FROM `product` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Product) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
    this.__updateAdapterOfProduct = object : EntityDeleteOrUpdateAdapter<Product>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `product` SET `id` = ?,`productName` = ?,`quantity` = ?,`departmentId` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Product) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.productName)
        statement.bindLong(3, entity.quantity.toLong())
        val _tmpDepartmentId: Int? = entity.departmentId
        if (_tmpDepartmentId == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmpDepartmentId.toLong())
        }
        statement.bindLong(5, entity.id.toLong())
      }
    }
  }

  public override suspend fun insert(vararg products: Product): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfProduct.insert(_connection, products)
  }

  public override suspend fun delete(product: Product): Unit = performSuspending(__db, false, true)
      { _connection ->
    __deleteAdapterOfProduct.handle(_connection, product)
  }

  public override suspend fun update(vararg products: Product): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfProduct.handleMultiple(_connection, products)
  }

  public override fun getAll(): Flow<List<Product>> {
    val _sql: String = "SELECT * FROM product ORDER BY productName ASC"
    return createFlow(__db, false, arrayOf("product")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfProductName: Int = getColumnIndexOrThrow(_stmt, "productName")
        val _columnIndexOfQuantity: Int = getColumnIndexOrThrow(_stmt, "quantity")
        val _columnIndexOfDepartmentId: Int = getColumnIndexOrThrow(_stmt, "departmentId")
        val _result: MutableList<Product> = mutableListOf()
        while (_stmt.step()) {
          val _item: Product
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpProductName: String
          _tmpProductName = _stmt.getText(_columnIndexOfProductName)
          val _tmpQuantity: Int
          _tmpQuantity = _stmt.getLong(_columnIndexOfQuantity).toInt()
          val _tmpDepartmentId: Int?
          if (_stmt.isNull(_columnIndexOfDepartmentId)) {
            _tmpDepartmentId = null
          } else {
            _tmpDepartmentId = _stmt.getLong(_columnIndexOfDepartmentId).toInt()
          }
          _item = Product(_tmpId,_tmpProductName,_tmpQuantity,_tmpDepartmentId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getProductDetails(): Flow<List<ProductDetail>> {
    val _sql: String = "SELECT * FROM productdetail"
    return createFlow(__db, true, arrayOf("productdetail")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDepartmentName: Int = getColumnIndexOrThrow(_stmt, "departmentName")
        val _columnIndexOfProductName: Int = getColumnIndexOrThrow(_stmt, "productName")
        val _columnIndexOfQuantity: Int = getColumnIndexOrThrow(_stmt, "quantity")
        val _result: MutableList<ProductDetail> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProductDetail
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpDepartmentName: String
          _tmpDepartmentName = _stmt.getText(_columnIndexOfDepartmentName)
          val _tmpProductName: String
          _tmpProductName = _stmt.getText(_columnIndexOfProductName)
          val _tmpQuantity: Int
          _tmpQuantity = _stmt.getLong(_columnIndexOfQuantity).toInt()
          _item = ProductDetail(_tmpId,_tmpDepartmentName,_tmpProductName,_tmpQuantity)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM product"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
