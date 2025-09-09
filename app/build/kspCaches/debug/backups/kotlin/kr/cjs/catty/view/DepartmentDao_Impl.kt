package kr.cjs.catty.view

import androidx.collection.LongSparseArray
import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndex
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.room.util.recursiveFetchLongSparseArray
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class DepartmentDao_Impl(
  __db: RoomDatabase,
) : DepartmentDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfDepartment: EntityInsertAdapter<Department>

  private val __deleteAdapterOfDepartment: EntityDeleteOrUpdateAdapter<Department>

  private val __updateAdapterOfDepartment: EntityDeleteOrUpdateAdapter<Department>
  init {
    this.__db = __db
    this.__insertAdapterOfDepartment = object : EntityInsertAdapter<Department>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `department` (`id`,`departmentName`) VALUES (nullif(?, 0),?)"

      protected override fun bind(statement: SQLiteStatement, entity: Department) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.departmentName)
      }
    }
    this.__deleteAdapterOfDepartment = object : EntityDeleteOrUpdateAdapter<Department>() {
      protected override fun createQuery(): String = "DELETE FROM `department` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Department) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
    this.__updateAdapterOfDepartment = object : EntityDeleteOrUpdateAdapter<Department>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `department` SET `id` = ?,`departmentName` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Department) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.departmentName)
        statement.bindLong(3, entity.id.toLong())
      }
    }
  }

  public override suspend fun insert(vararg departments: Department): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfDepartment.insert(_connection, departments)
  }

  public override suspend fun delete(department: Department): Unit = performSuspending(__db, false,
      true) { _connection ->
    __deleteAdapterOfDepartment.handle(_connection, department)
  }

  public override suspend fun update(vararg departments: Department): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfDepartment.handleMultiple(_connection, departments)
  }

  public override fun getAll(): Flow<List<Department>> {
    val _sql: String = "SELECT * FROM department ORDER BY departmentName ASC"
    return createFlow(__db, false, arrayOf("department")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDepartmentName: Int = getColumnIndexOrThrow(_stmt, "departmentName")
        val _result: MutableList<Department> = mutableListOf()
        while (_stmt.step()) {
          val _item: Department
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpDepartmentName: String
          _tmpDepartmentName = _stmt.getText(_columnIndexOfDepartmentName)
          _item = Department(_tmpId,_tmpDepartmentName)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun selectDepartmentWithProducts(departmentName: String):
      Flow<List<DepartmentWithProduct>> {
    val _sql: String = "SELECT * FROM department WHERE departmentName = ?"
    return createFlow(__db, true, arrayOf("product", "department")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, departmentName)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDepartmentName: Int = getColumnIndexOrThrow(_stmt, "departmentName")
        val _collectionProducts: LongSparseArray<MutableList<Product>> =
            LongSparseArray<MutableList<Product>>()
        while (_stmt.step()) {
          val _tmpKey: Long
          _tmpKey = _stmt.getLong(_columnIndexOfId)
          if (!_collectionProducts.containsKey(_tmpKey)) {
            _collectionProducts.put(_tmpKey, mutableListOf())
          }
        }
        _stmt.reset()
        __fetchRelationshipproductAskrCjsCattyViewProduct(_connection, _collectionProducts)
        val _result: MutableList<DepartmentWithProduct> = mutableListOf()
        while (_stmt.step()) {
          val _item: DepartmentWithProduct
          val _tmpDepartment: Department
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpDepartmentName: String
          _tmpDepartmentName = _stmt.getText(_columnIndexOfDepartmentName)
          _tmpDepartment = Department(_tmpId,_tmpDepartmentName)
          val _tmpProductsCollection: MutableList<Product>
          val _tmpKey_1: Long
          _tmpKey_1 = _stmt.getLong(_columnIndexOfId)
          _tmpProductsCollection = checkNotNull(_collectionProducts.get(_tmpKey_1))
          _item = DepartmentWithProduct(_tmpDepartment,_tmpProductsCollection)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM department"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  private fun __fetchRelationshipproductAskrCjsCattyViewProduct(_connection: SQLiteConnection,
      _map: LongSparseArray<MutableList<Product>>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, true) { _tmpMap ->
        __fetchRelationshipproductAskrCjsCattyViewProduct(_connection, _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`productName`,`quantity`,`departmentId` FROM `product` WHERE `departmentId` IN (")
    val _inputSize: Int = _map.size()
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    val _stmt: SQLiteStatement = _connection.prepare(_sql)
    var _argIndex: Int = 1
    for (i in 0 until _map.size()) {
      val _item: Long = _map.keyAt(i)
      _stmt.bindLong(_argIndex, _item)
      _argIndex++
    }
    try {
      val _itemKeyIndex: Int = getColumnIndex(_stmt, "departmentId")
      if (_itemKeyIndex == -1) {
        return
      }
      val _columnIndexOfId: Int = 0
      val _columnIndexOfProductName: Int = 1
      val _columnIndexOfQuantity: Int = 2
      val _columnIndexOfDepartmentId: Int = 3
      while (_stmt.step()) {
        val _tmpKey: Long?
        if (_stmt.isNull(_itemKeyIndex)) {
          _tmpKey = null
        } else {
          _tmpKey = _stmt.getLong(_itemKeyIndex)
        }
        if (_tmpKey != null) {
          val _tmpRelation: MutableList<Product>? = _map.get(_tmpKey)
          if (_tmpRelation != null) {
            val _item_1: Product
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
            _item_1 = Product(_tmpId,_tmpProductName,_tmpQuantity,_tmpDepartmentId)
            _tmpRelation.add(_item_1)
          }
        }
      }
    } finally {
      _stmt.close()
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
