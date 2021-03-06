package design_pattern.iterator;

/**
 * 聚合接口类
 */
public interface Aggregate {

    /**
     * 创建迭代器
     * @return
     */
    Iterator createIterator();
}
