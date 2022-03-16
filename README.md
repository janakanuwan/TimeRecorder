# TimeRecorder
Android app to record time during mobile experiment
- Record the time during temporary events (obstacles, OBSTA), and fixed events (STAND, LIFT)
- Click `Timer` to start the timer, and then click
- Data are saved in `Downloads > TimerRecord` directory with the given `FILE_NAME` while exporting (`<FILE_NAME>.csv`)
- Data are in CSV format (`<EVENT>,<SECONDS>\n`)
    - sample
    ```
    TIMER_START,0
    OBSTACLE,6
    OBSTACLE,13
    STAIR_START,16
    STAIR_STOP,19
    TIMER_STOP,23
    ```
- User interfaces ![Initial UI](screenshots/UI_initial.jpg), ![Export UI](screenshots/UI_export.jpg)

## How to run?
1. Build the project first (Make Tool)
2. Run the app

