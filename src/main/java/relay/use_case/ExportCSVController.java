package relay.use_case;

public class ExportCSVController {

    final ExportCSVInputBoundary exportCSVInteractor;

    public ExportCSVController(ExportCSVInputBoundary exportCSVInteractor) {
        this.exportCSVInteractor = exportCSVInteractor;
    }

    public void execute(ExportCSVInputData inputData) {
        exportCSVInteractor.execute(inputData);

    }
}
