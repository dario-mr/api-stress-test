package com.dario.ast.view;

import com.dario.ast.core.domain.IndexedHttpStatus;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ListDataProvider;

public class ResultGrid extends Grid<IndexedHttpStatus> {

    public ResultGrid(ListDataProvider<IndexedHttpStatus> dataProvider) {
        setDataProvider(dataProvider);

        addColumn(IndexedHttpStatus::index).setHeader("#");
        addColumn(item -> item.httpStatus().value()).setHeader("Status");
        addColumn(item -> item.httpStatus().getReasonPhrase()).setHeader("Description");
    }
}
