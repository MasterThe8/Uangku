package pember.latihan.uangku.model.seed

import pember.latihan.uangku.model.Category

object CategorySeed {
    val defaultCategories = listOf(
        Category(name = "Uang Kaget", type = "income"),
        Category(name = "Gaji", type = "income"),
        Category(name = "Hadiah", type = "income"),
        Category(name = "Uang Saku", type = "income"),
        Category(name = "Pendapatan Lainnya", type = "income"),
        Category(name = "Makanan", type = "expense"),
        Category(name = "Belanja", type = "expense"),
        Category(name = "Liburan", type = "expense"),
        Category(name = "Transportasi", type = "expense"),
        Category(name = "Tagihan", type = "expense"),
        Category(name = "Pendidikan", type = "expense"),
        Category(name = "Kesehatan", type = "expense"),
        Category(name = "Lainnya", type = "expense")
    )
}
