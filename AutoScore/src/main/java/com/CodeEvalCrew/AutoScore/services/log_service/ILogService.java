package com.CodeEvalCrew.AutoScore.services.log_service;

import java.io.IOException;

public interface ILogService {
     String exportLogToFile(Long examPaperId) throws IOException;
}