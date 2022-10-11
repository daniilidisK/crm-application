/*
 *  Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package impl.com.calendarfx.view;

import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.AllDayView;
import com.calendarfx.view.TimeScaleView;
import com.calendarfx.view.WeekDayHeaderView;
import com.calendarfx.model.Resource;
import com.calendarfx.view.ResourcesView;
import com.calendarfx.view.ResourcesView.Type;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.time.LocalDate;

import static com.calendarfx.util.ViewHelper.scrollToRequestedTime;

public class ResourcesViewSkin<T extends Resource<?>> extends DateControlSkin<ResourcesView<T>> {

    private final GridPane gridPane;
    private final ResourcesViewContainer<T> resourcesContainer;
    private final DayViewScrollPane timeScaleScrollPane;
    private final DayViewScrollPane dayViewsScrollPane;
    private final ScrollBar scrollBar;

    public ResourcesViewSkin(ResourcesView<T> view) {
        super(view);

        scrollBar = new ScrollBar();
        scrollBar.setOrientation(Orientation.VERTICAL);

        TimeScaleView timeScale = new TimeScaleView();
        view.bind(timeScale, true);

        // time scale scroll pane
        timeScaleScrollPane = new DayViewScrollPane(timeScale, scrollBar);
        timeScaleScrollPane.getStyleClass().addAll("calendar-scroll-pane", "day-view-timescale-scroll-pane");
        timeScaleScrollPane.setMinWidth(Region.USE_PREF_SIZE);

        InvalidationListener updateViewListener = it -> updateView();
        view.showAllDayViewProperty().addListener(updateViewListener);
        view.showTimeScaleViewProperty().addListener(updateViewListener);
        view.layoutProperty().addListener(updateViewListener);
        view.showScrollBarProperty().addListener(updateViewListener);
        view.showTimeScaleViewProperty().addListener(updateViewListener);
        view.numberOfDaysProperty().addListener(updateViewListener);
        view.typeProperty().addListener(updateViewListener);
        view.getResources().addListener(updateViewListener);

        RowConstraints row0 = new RowConstraints();
        row0.setFillHeight(true);
        row0.setPrefHeight(Region.USE_COMPUTED_SIZE);
        row0.setVgrow(Priority.NEVER);

        RowConstraints row1 = new RowConstraints();
        row1.setFillHeight(true);
        row1.setPrefHeight(Region.USE_COMPUTED_SIZE);
        row1.setVgrow(Priority.ALWAYS);

        gridPane = new GridPane();
        gridPane.getRowConstraints().setAll(row0, row1);
        gridPane.getStyleClass().add("container");

        resourcesContainer = new ResourcesViewContainer<>(view);

        view.bind(resourcesContainer, true);

        getChildren().add(gridPane);

        dayViewsScrollPane = new DayViewScrollPane(resourcesContainer, scrollBar);

        /*
         * Run later when the control has become visible.
         */
        Platform.runLater(() -> scrollToRequestedTime(view, dayViewsScrollPane));

        view.requestedTimeProperty().addListener(it -> scrollToRequestedTime(view, dayViewsScrollPane));

        updateView();
    }


    private void updateView() {
        gridPane.getChildren().clear();
        gridPane.getColumnConstraints().clear();

        ResourcesView<T> view = getSkinnable();
        if (view.getType().equals(Type.RESOURCES_OVER_DATES)) {
            updateViewResourcesOverDates();
        } else {
            updateViewDatesOverResources();
        }
    }

    private void updateViewDatesOverResources() {
        final ResourcesView<T> view = getSkinnable();

        if (view.isShowTimeScaleView()) {
            ColumnConstraints timeScaleColumn = new ColumnConstraints();
            timeScaleColumn.setFillWidth(true);
            timeScaleColumn.setHgrow(Priority.NEVER);
            gridPane.getColumnConstraints().add(timeScaleColumn);
            gridPane.add(timeScaleScrollPane, 0, 1);

            Node upperLeftCorner = view.getUpperLeftCorner();
            upperLeftCorner.getStyleClass().add("upper-left-corner");
            gridPane.add(upperLeftCorner, 0, 0);
        }

        if (view.isShowScrollBar()) {
            Node upperRightCorner = view.getUpperRightCorner();
            upperRightCorner.getStyleClass().add("upper-right-corner");
            gridPane.add(upperRightCorner, 2, 0);
        }

        Callback<T, Node> resourceHeaderFactory = view.getResourceHeaderFactory();

        ObservableList<T> resources = view.getResources();

        HBox headerBox = new HBox();
        headerBox.getStyleClass().add("header-box");

        gridPane.add(headerBox, 1, 0);

        for (int dayIndex = 0; dayIndex < view.getNumberOfDays(); dayIndex++) {
            ObjectProperty<LocalDate> dateProperty = new SimpleObjectProperty<>(this, "date");
            final int additionalDays = dayIndex;
            dateProperty.bind(Bindings.createObjectBinding(() -> view.getDate().plusDays(additionalDays), view.dateProperty()));

            VBox dayBox = new VBox();
            dayBox.getStyleClass().add("day-box");
            HBox.setHgrow(dayBox, Priority.ALWAYS);

            WeekDayHeaderView weekDayHeaderView = new WeekDayHeaderView();
            view.bind(weekDayHeaderView, false);
            weekDayHeaderView.dateProperty().bind(dateProperty);
            weekDayHeaderView.setNumberOfDays(1);
            weekDayHeaderView.setAdjustToFirstDayOfWeek(false);

            weekDayHeaderView.getStyleClass().removeAll("only", "first", "middle", "last");

            if (view.getNumberOfDays() == 1) {
                weekDayHeaderView.getStyleClass().add("only");
            } else {
                if (dayIndex == 0) {
                    weekDayHeaderView.getStyleClass().add("first");
                } else if (dayIndex == view.getNumberOfDays() - 1) {
                    weekDayHeaderView.getStyleClass().add("last");
                } else {
                    weekDayHeaderView.getStyleClass().add("middle");
                }
            }

            dayBox.getChildren().add(weekDayHeaderView);

            HBox allResourcesBox = new HBox();
            VBox.setVgrow(allResourcesBox, Priority.ALWAYS);

            dayBox.getChildren().add(allResourcesBox);

            headerBox.getChildren().add(dayBox);

            // separator between dates
            if (dayIndex < view.getNumberOfDays() - 1) {
                Callback<ResourcesView<T>, Region> separatorFactory = view.getLargeSeparatorFactory();
                if (separatorFactory != null) {
                    Region separator = separatorFactory.call(view);
                    if (separator != null) {
                        headerBox.getChildren().add(separator);
                        HBox.setHgrow(separator, Priority.NEVER);
                    }
                }
            }

            for (int resourceIndex = 0; resourceIndex < resources.size(); resourceIndex++) {
                T resource = resources.get(resourceIndex);

                Node resourceHeaderNode = resourceHeaderFactory.call(resource);

                VBox singleResourceBox = new VBox(resourceHeaderNode);
                HBox.setHgrow(singleResourceBox, Priority.ALWAYS);

                allResourcesBox.getChildren().add(singleResourceBox);

                resourceHeaderNode.getStyleClass().removeAll("only", "first", "middle", "last");

                if (resources.size() == 1) {
                    resourceHeaderNode.getStyleClass().add("only");
                } else {
                    if (resourceIndex == 0) {
                        resourceHeaderNode.getStyleClass().add("first");
                    } else if (resourceIndex == resources.size() - 1) {
                        resourceHeaderNode.getStyleClass().add("last");
                    } else {
                        resourceHeaderNode.getStyleClass().add("middle");
                    }
                }

                if (view.isShowAllDayView()) {

                    Callback<T, AllDayView> allDayViewFactory = view.getAllDayViewFactory();
                    AllDayView allDayView = allDayViewFactory.call(resource);

                    allDayView.getStyleClass().removeAll("only", "first", "middle", "last");

                    if (resources.size() == 1) {
                        allDayView.getStyleClass().add("only");
                    } else {
                        if (resourceIndex == 0) {
                            allDayView.getStyleClass().add("first");
                        } else if (resourceIndex == resources.size() - 1) {
                            allDayView.getStyleClass().add("last");
                        } else {
                            allDayView.getStyleClass().add("middle");
                        }
                    }

                    // bind AllDayView
                    view.bind(allDayView, false);

                    Bindings.unbindBidirectional(view.adjustToFirstDayOfWeekProperty(), allDayView.adjustToFirstDayOfWeekProperty());
                    Bindings.unbindBidirectional(view.numberOfDaysProperty(), allDayView.numberOfDaysProperty());

                    allDayView.dateProperty().bind(dateProperty);
                    allDayView.setAdjustToFirstDayOfWeek(false);
                    allDayView.setNumberOfDays(1);

                    // some unbindings for AllDayView
                    Bindings.unbindBidirectional(view.defaultCalendarProviderProperty(), allDayView.defaultCalendarProviderProperty());
                    Bindings.unbindBidirectional(view.draggedEntryProperty(), allDayView.draggedEntryProperty());
                    Bindings.unbindContentBidirectional(view.getCalendarSources(), allDayView.getCalendarSources());

                    CalendarSource calendarSource = createCalendarSource(resource);
                    allDayView.getCalendarSources().setAll(calendarSource);
                    allDayView.setDefaultCalendarProvider(control -> calendarSource.getCalendars().get(0));

                    VBox.setVgrow(allDayView, Priority.ALWAYS);
                    singleResourceBox.getChildren().add(allDayView);
                }

                singleResourceBox.setPrefWidth(0); // so they all end up with the same percentage width

                HBox.setHgrow(singleResourceBox, Priority.ALWAYS);

                if (resourceIndex < resources.size() - 1) {
                    Callback<ResourcesView<T>, Region> separatorFactory = view.getSmallSeparatorFactory();
                    if (separatorFactory != null) {
                        Region separator = separatorFactory.call(view);
                        if (separator != null) {
                            allResourcesBox.getChildren().add(separator);
                            HBox.setHgrow(separator, Priority.NEVER);
                        }
                    }
                }
            }
        }

        ColumnConstraints dayViewsConstraints = new ColumnConstraints();
        dayViewsConstraints.setFillWidth(true);
        dayViewsConstraints.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().add(dayViewsConstraints);
        gridPane.add(dayViewsScrollPane, 1, 1);

        if (view.isShowScrollBar()) {
            ColumnConstraints scrollbarConstraint = new ColumnConstraints();
            scrollbarConstraint.setFillWidth(true);
            scrollbarConstraint.setHgrow(Priority.NEVER);
            scrollbarConstraint.setPrefWidth(Region.USE_COMPUTED_SIZE);
            gridPane.getColumnConstraints().add(scrollbarConstraint);

            gridPane.add(scrollBar, 2, 1);
        }
    }

    private void updateViewResourcesOverDates() {
        final ResourcesView<T> view = getSkinnable();

        if (view.isShowTimeScaleView()) {
            ColumnConstraints timeScaleColumn = new ColumnConstraints();
            timeScaleColumn.setFillWidth(true);
            timeScaleColumn.setHgrow(Priority.NEVER);
            gridPane.getColumnConstraints().add(timeScaleColumn);

            gridPane.add(timeScaleScrollPane, 0, 1);

            Node upperLeftCorner = view.getUpperLeftCorner();
            upperLeftCorner.getStyleClass().add("upper-left-corner");
            gridPane.add(upperLeftCorner, 0, 0);
        }

        if (view.isShowScrollBar()) {
            Node upperRightCorner = view.getUpperRightCorner();
            upperRightCorner.getStyleClass().add("upper-right-corner");
            gridPane.add(upperRightCorner, 2, 0);
        }

        HBox headerBox = new HBox();
        headerBox.getStyleClass().add("header-box");

        gridPane.add(headerBox, 1, 0);

        Callback<T, Node> resourceHeaderFactory = view.getResourceHeaderFactory();

        ObservableList<T> resources = view.getResources();
        for (int i = 0; i < resources.size(); i++) {
            T resource = resources.get(i);

            Node headerNode = resourceHeaderFactory.call(resource);

            VBox resourceHeader = new VBox(headerNode);
            resourceHeader.getStyleClass().removeAll("only", "first", "middle", "last");

            if (resources.size() == 1) {
                resourceHeader.getStyleClass().add("only");
            } else {
                if (i == 0) {
                    resourceHeader.getStyleClass().add("first");
                } else if (i == resources.size() - 1) {
                    resourceHeader.getStyleClass().add("last");
                } else {
                    resourceHeader.getStyleClass().add("middle");
                }
            }

            if (view.isShowAllDayView()) {
                AllDayView allDayView = new AllDayView();
                allDayView.setAdjustToFirstDayOfWeek(false);

                // bind AllDayView
                view.bind(allDayView, true);
                allDayView.numberOfDaysProperty().bind(view.numberOfDaysProperty());

                // rebind
                allDayView.adjustToFirstDayOfWeekProperty().bind(view.adjustToFirstDayOfWeekProperty());

                // some unbindings for AllDayView
                Bindings.unbindBidirectional(view.defaultCalendarProviderProperty(), allDayView.defaultCalendarProviderProperty());
                Bindings.unbindBidirectional(view.draggedEntryProperty(), allDayView.draggedEntryProperty());
                Bindings.unbindContentBidirectional(view.getCalendarSources(), allDayView.getCalendarSources());

                CalendarSource calendarSource = createCalendarSource(resource);
                allDayView.getCalendarSources().setAll(calendarSource);
                allDayView.setDefaultCalendarProvider(control -> calendarSource.getCalendars().get(0));

                resourceHeader.getChildren().add(allDayView);
            }

            resourceHeader.getStyleClass().add("resource-header-view");

            WeekDayHeaderView weekDayHeaderView = view.getWeekDayHeaderViewFactory().call(resource);
            weekDayHeaderView.adjustToFirstDayOfWeekProperty().bind(view.adjustToFirstDayOfWeekProperty());
            weekDayHeaderView.numberOfDaysProperty().bind(view.numberOfDaysProperty());
            view.bind(weekDayHeaderView, true);

            resourceHeader.setPrefWidth(0); // so they all end up with the same percentage width
            resourceHeader.getChildren().add(weekDayHeaderView);

            headerBox.getChildren().add(resourceHeader);
            HBox.setHgrow(resourceHeader, Priority.ALWAYS);

            if (i < resources.size() - 1) {
                Callback<ResourcesView<T>, Region> separatorFactory = view.getLargeSeparatorFactory();
                if (separatorFactory != null) {
                    Region separator = separatorFactory.call(view);
                    if (separator != null) {
                        headerBox.getChildren().add(separator);
                        HBox.setHgrow(separator, Priority.NEVER);
                    }
                }
            }
        }

        ColumnConstraints dayViewsConstraints = new ColumnConstraints();
        dayViewsConstraints.setFillWidth(true);
        dayViewsConstraints.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().add(dayViewsConstraints);
        gridPane.add(dayViewsScrollPane, 1, 1);

        if (view.isShowScrollBar()) {
            ColumnConstraints scrollbarConstraint = new ColumnConstraints();
            scrollbarConstraint.setFillWidth(true);
            scrollbarConstraint.setHgrow(Priority.NEVER);
            scrollbarConstraint.setPrefWidth(Region.USE_COMPUTED_SIZE);
            gridPane.getColumnConstraints().add(scrollbarConstraint);

            gridPane.add(scrollBar, 2, 1);
        }
    }

    private CalendarSource createCalendarSource(T resource) {
        CalendarSource source = new CalendarSource(resource.getUserObject().toString());
        source.getCalendars().add(resource.getCalendar());
        return source;
    }
}