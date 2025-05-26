# TpGestionEvenementPoo2
This project intend to apply multiple OOP concepts such as : design pattern,custom exception handling,JSON/XML serialisation,lamdas/streams ,asynchronous program and using JUNIT in order to test .
we did use in our case :
- JSON serialisation ,lambdas ,asynchronous programming ,customed exception handling ,design patterns and JUNIT .
-----------------------------------------------------------TOOLS OF THE PROJECT --------------------------------------------------------------------------------------
- GRADLE
- JSON
- JAVA 21
- 
------------------------------------------------------REMARKS--------------------------------------------------------------------------------------------
  This project we did not use the NotificationService interface because , its action was already implemented in the observer pattern via :
- EvenementObservable
- ParticipantObserver
we have to understand that : When an event is canceled  it directly calls the update() method on its registered ParticipantObserver instances.The Participant.update() method then directly triggers its own receiveNotification() method, which handles the asynchronous notification.
