# QuiltKeeper
This is one of my latest applications. It takes full advantage of the MVVM architecture alongside various Firebase tools including the Realtime Database, storage, authentication, and remote config. It has resulted in perhaps the crispiest and cleanest code I have written to date.

The remote config tool is used to automatically inform users when an update has been made available.

The function of the application is to allow users to manage quilting projects by keeping track of different equipment used and types of information associated with the project such as stich count, types of thread or badding used and other data. Users can take a photograph of their finished product, calculate costs and edit entries.

All of the quilts are displayed in a Recycler view which uses a custom item click listener and features a swipe to delete function. They can utilize long presses to copy an entry. Other features include the ability to delete photos on storage upon deletion of an entry in the RTDB, the ability to dynamically add or delete item selections for the various spinners (drop down menus) used, select a default for the drop down menus as well as track total costs and sales of their quilt projects. 
