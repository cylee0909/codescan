package com.cylee.androidlib.base;

public interface Callback<ResultType> {
	/**
	 * 在调用时所在的线程执行回调处理（比如事件发起的调用是在ui线程，回调就可以直接操作ui元素）
	 * @param data 经过自动数据转换后的对象， 如，我们要求一个JavaBean，他会自动构造该对象， 并且递归初始化他的属性（从json属性值自动转换类型并赋值），支持范型
	 */
	 void callback(ResultType data);
}

