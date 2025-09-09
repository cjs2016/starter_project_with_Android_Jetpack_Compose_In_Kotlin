package kr.cjs.catty.viewmodel

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kr.cjs.catty.view.CattyDatabase
import kr.cjs.catty.view.Department
import kr.cjs.catty.view.DepartmentDao
import kr.cjs.catty.view.Product
import kr.cjs.catty.view.ProductDao
import kr.cjs.catty.view.ProductDetail



class SuppliesRepository(private val productDao: ProductDao, private val departmentDao: DepartmentDao) {

    val allProducts: Flow<List<Product>> = productDao.getAll()
    val allDepartments: Flow<List<Department>> = departmentDao.getAll()

    val allProductDetail: Flow<List<ProductDetail>> = productDao.getProductDetails()


    suspend fun insertProduct(product: Product) {
        productDao.insert(product)
    }

    suspend fun insertDepartment(department: Department) {
        departmentDao.insert(department)
    }

    suspend fun updateProduct(product: Product) {
        productDao.update(product)
    }

    suspend fun updateDepartment(department: Department) {
        departmentDao.update(department)
    }


    suspend fun deleteProduct(product: Product) {
        productDao.delete(product)
    }


    suspend fun deleteDepartment(department: Department) {
        departmentDao.delete(department)
    }

    suspend fun deleteAllProduct(){
        productDao.deleteAll()
    }

    suspend fun deleteAllDepartment(){
        departmentDao.deleteAll()
    }



}

class SuppliesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SuppliesRepository
    val allProducts: Flow<List<Product>>

    val allProductDetail: Flow<List<ProductDetail>>
    val allDepartments: Flow<List<Department>>

    init {
        val database = CattyDatabase.getDatabase(application, viewModelScope)
        val productDao = database.ProductDao()
        val departmentDao = database.DepartmentDao()
        repository = SuppliesRepository(productDao, departmentDao)
        allProducts = repository.allProducts
        allDepartments = repository.allDepartments
        allProductDetail = repository.allProductDetail

    }


    fun insertProduct(product: Product) = viewModelScope.launch {
        repository.insertProduct(product)
    }

    fun insertDepartment(department: Department) = viewModelScope.launch {
        repository.insertDepartment(department)
    }

    fun updateProduct(product: Product) = viewModelScope.launch {
        repository.updateProduct(product)
    }

    fun updateDepartment(department: Department) = viewModelScope.launch {
        repository.updateDepartment(department)
    }


    fun deleteProduct(product: Product) = viewModelScope.launch {
        repository.deleteProduct(product)
    }

    fun deleteDepartment(department: Department) = viewModelScope.launch {
        repository.deleteDepartment(department)
    }

    fun deleteAllProduct() = viewModelScope.launch {
        repository.deleteAllProduct()
    }

    fun deleteAllDepartment() = viewModelScope.launch {
        repository.deleteAllDepartment()
    }


    companion object {

        var productId by mutableStateOf(0)
        var productName by mutableStateOf("")
        var quantity by mutableStateOf("")
        var selectedDepartment by mutableStateOf<Department?>(null)

        var updateState by mutableStateOf(false)

        fun Factory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.AndroidViewModelFactory(application) {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SuppliesViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return SuppliesViewModel(application) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }

}
