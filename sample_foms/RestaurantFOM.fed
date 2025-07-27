(fom
  (objectClasses
    (Restaurant
      (attributes
        (ID "Integer")
        (Name "String")
        (Location "String")
        (Capacity "Integer")
        (OpenStatus "Boolean")
        (CurrentOrders "Integer")
        (Revenue "Float")
        (MenuItems "ListOf MenuItem")
      )
    )
    (MenuItem
      (attributes
        (ID "Integer")
        (Name "String")
        (Category "String")
        (Price "Float")
        (PreparationTime "Float")
        (Ingredients "ListOf Ingredient")
      )
    )
    (Ingredient
      (attributes
        (ID "Integer")
        (Name "String")
        (QuantityAvailable "Float")
        (Unit "String")
        (Cost "Float")
      )
    )
    (Customer
      (attributes
        (ID "Integer")
        (Name "String")
        (PhoneNumber "String")
        (Email "String")
        (PreferredPayment "String")
        (OrderHistory "ListOf Order")
      )
    )
    (Order
      (attributes
        (ID "Integer")
        (CustomerID "Integer")
        (Items "ListOf OrderItem")
        (TotalAmount "Float")
        (OrderTime "Time")
        (Status "String")
      )
    )
    (OrderItem
      (attributes
        (MenuItemID "Integer")
        (Quantity "Integer")
        (SpecialRequests "String")
      )
    )
    (Employee
      (attributes
        (ID "Integer")
        (Name "String")
        (Role "String")
        (ShiftStart "Time")
        (ShiftEnd "Time")
        (HourlyRate "Float")
        (AssignedOrders "ListOf Order")
      )
    )
    (Table
      (attributes
        (ID "Integer")
        (Seats "Integer")
        (Occupied "Boolean")
        (AssignedOrderID "Integer")
      )
    )
    (Reservation
      (attributes
        (ID "Integer")
        (CustomerID "Integer")
        (TableID "Integer")
        (ReservationTime "Time")
        (SpecialRequests "String")
        (Status "String")
      )
    )
  )

  (interactionClasses
    (PlaceOrder
      (parameters
        (CustomerID "Integer")
        (Items "ListOf OrderItem")
        (TotalAmount "Float")
        (OrderTime "Time")
      )
    )
    (UpdateOrderStatus
      (parameters
        (OrderID "Integer")
        (Status "String")
      )
    )
    (AssignTable
      (parameters
        (OrderID "Integer")
        (TableID "Integer")
      )
    )
    (AssignEmployee
      (parameters
        (OrderID "Integer")
        (EmployeeID "Integer")
        (Role "String")
      )
    )
    (ProcessPayment
      (parameters
        (OrderID "Integer")
        (Amount "Float")
        (PaymentMethod "String")
      )
    )
    (UpdateInventory
      (parameters
        (IngredientID "Integer")
        (QuantityUsed "Float")
      )
    )
    (UpdateMenuItem
      (parameters
        (MenuItemID "Integer")
        (NewPrice "Float")
        (NewPreparationTime "Float")
      )
    )
    (CreateReservation
      (parameters
        (CustomerID "Integer")
        (TableID "Integer")
        (ReservationTime "Time")
        (SpecialRequests "String")
      )
    )
    (CancelReservation
      (parameters
        (ReservationID "Integer")
      )
    )
  )
)