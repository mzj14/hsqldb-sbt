# hsqldb-sbt

本项目将HSQLDB的AVL树索引更改为SBT树的索引，对HSQLDB-2.3.0源代码的更改如下:

1. 将源代码中的所有"AVL"字样替换成了"SBT"(包括文件名, 类名，函数名和变量名)

2. 对 org.hsqldb.index.IndexSBT 和 org.hsqldb.index.IndexSBTMemory 的成员函数进行重写，主要包括:

   2.1 重写内部函数 balance, 并增加了内部函数 largeThanLeftChild, leftRotate, largeThanRightChild, rightRotate

   2.2 修改内部函数 delete, 去除了删除过程中的平衡操作(SBT树的删除过程可以不进行平衡操作)
