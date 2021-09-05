package com.example.gifapp.ext

fun <T> fastLazy(initializer: () -> T): Lazy<T> = lazy(
    initializer = initializer,
    mode = LazyThreadSafetyMode.NONE
)