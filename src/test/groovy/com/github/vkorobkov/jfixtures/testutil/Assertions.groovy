package com.github.vkorobkov.jfixtures.testutil

trait Assertions {

    def assertCollectionsEqual(Collection left, Collection right) {
        assert left.size() == right.size()
        def rightIterator = right.iterator()
        left.eachWithIndex { def leftElement, int index ->
            def rightElement = rightIterator.next()
            assert leftElement == rightElement :
                "Collections have different elements at index $index: left: $leftElement, right: $rightElement"
        }
    }
}