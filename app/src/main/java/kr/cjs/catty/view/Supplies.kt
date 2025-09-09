package kr.cjs.catty.view

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.DatabaseView
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch



@Entity(tableName = "department")
data class Department(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val departmentName: String
)


@Entity(
    tableName = "product",
    foreignKeys = [
        ForeignKey(
            entity = Department::class,
            parentColumns = ["id"],
            childColumns = ["departmentId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productName: String,
    val quantity: Int,
    @ColumnInfo(index = true)
    val departmentId: Int? = null
)


data class DepartmentWithProduct(
    @Embedded val department: Department,
    @Relation(
        parentColumn = "id",
        entityColumn = "departmentId"
    )
    val products: List<Product>
)

@DatabaseView("SELECT p.id, d.departmentName, p.productName, p.quantity " +
        "FROM department as d INNER JOIN product as p " +
        "ON d.id = p.departmentId ORDER BY p.id DESC")
data class ProductDetail(
    val id: Int,
    val departmentName: String,
    val productName: String,
    val quantity: Int
)

@Dao
interface ProductDao {
    @Query("SELECT * FROM product ORDER BY productName ASC")
    fun getAll(): Flow<List<Product>>

    @Transaction
    @Query("SELECT * FROM productdetail")
    fun getProductDetails(): Flow<List<ProductDetail>>

    @Query("DELETE FROM product")
    suspend fun deleteAll()

    @Insert
    suspend fun insert(vararg products: Product)

    @Update
    suspend fun update(vararg products: Product)

    @Delete
    suspend fun delete(product: Product)


}

@Dao
interface DepartmentDao {
    @Query("SELECT * FROM department ORDER BY departmentName ASC")
    fun getAll(): Flow<List<Department>>

    @Query("DELETE FROM department")
    suspend fun deleteAll()

    @Transaction
    @Query("SELECT * FROM department WHERE departmentName = :departmentName")
    fun selectDepartmentWithProducts(departmentName: String): Flow<List<DepartmentWithProduct>>

    @Insert
    suspend fun insert(vararg departments: Department)

    @Update
    suspend fun update(vararg departments: Department)

    @Delete
    suspend fun delete(department: Department)


}

@Database(
    entities = [Product::class, Department::class],
    views = [ProductDetail::class],
    version = 1,
    exportSchema = false
)
abstract class CattyDatabase : RoomDatabase() {
    abstract fun ProductDao(): ProductDao
    abstract fun DepartmentDao(): DepartmentDao

    companion object {
        // @Volatile ensures that the value of INSTANCE is always up-to-date and the same for all execution threads.
        @Volatile
        private var INSTANCE: CattyDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): CattyDatabase {
            // Return the existing instance if it exists, otherwise create a new one inside a synchronized block.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CattyDatabase::class.java,
                    "supplies.db"
                )
                .addCallback(CattyDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }

        private class CattyDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateDatabase(database.DepartmentDao(), database.ProductDao())
                    }
                }
            }

            suspend fun populateDatabase(departmentDao: DepartmentDao, productDao: ProductDao) {
                departmentDao.insert(Department(departmentName = "고양이 장난감"))
                departmentDao.insert(Department(departmentName = "캣타워"))
                departmentDao.insert(Department(departmentName = "고양이 간식"))

                productDao.insert(Product(productName = "깃털", quantity = 50, departmentId = 1))
                productDao.insert(Product(productName = "공놀이기구", quantity = 10000, departmentId = 1))
                productDao.insert(Product(productName = "2층타워", quantity = 5, departmentId = 2))
                productDao.insert(Product(productName = "5층타워", quantity = 5000, departmentId = 2))
                productDao.insert(Product(productName = "내츄럴코어", quantity = 500, departmentId = 3))
                productDao.insert(Product(productName = "팜스코 캣츠비", quantity = 500, departmentId = 3))
            }
        }
    }


}