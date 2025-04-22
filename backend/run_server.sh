#!/bin/bash

if [ "$1" == "--host" ]; then
    echo "Running backend with --host 0.0.0.0"
    # Activate the virtual environment and run uvicorn with --host 0.0.0.0
    source .venv/bin/activate
    uvicorn main:app --reload --host 0.0.0.0
else
    echo "Running backend with default settings"
    # Activate the virtual environment and run uvicorn without --host 0.0.0.0
    source .venv/bin/activate
    uvicorn main:app --reload
fi
