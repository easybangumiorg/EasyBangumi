#include <unwind.h>
#include <dlfcn.h>
#include <iomanip>
#include <sstream>
#include <android/log.h>


struct BacktraceState {
    void **current;
    void **end;
};


static _Unwind_Reason_Code unwindCallback(struct _Unwind_Context *context, void *args) {
    BacktraceState *state = static_cast<BacktraceState *>(args);
    uintptr_t pc = _Unwind_GetIP(context);
    if (pc) {
        if (state->current == state->end) {
            return _URC_END_OF_STACK;
        } else {
            *state->current++ = reinterpret_cast<void *>(pc);
        }
    }
    return _URC_NO_REASON;
}


size_t captureBacktrace(void **buffer, size_t max) {
    BacktraceState state = {buffer, buffer + max};
    _Unwind_Backtrace(unwindCallback, &state);
    // 获取大小
    return state.current - buffer;
}


void dumpBacktrace(std::ostream &os, void **buffer, size_t count) {
    for (size_t idx = 0; idx < count; ++idx) {
        const void *addr = buffer[idx];
        const char *symbol = "";
        Dl_info info;
        if (dladdr(addr, &info) && info.dli_sname) {
            symbol = info.dli_sname;
        }
        os << "  #" << std::setw(2) << idx << ": " << addr << "  " << symbol << "\n";
    }
}


std::string backtraceToLogcat() {
    const size_t max = 30;
    void *buffer[max];
    std::ostringstream oss;


    dumpBacktrace(oss, buffer, captureBacktrace(buffer, max));
    return oss.str();
}



